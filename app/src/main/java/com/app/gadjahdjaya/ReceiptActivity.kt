// ReceiptActivity.kt
package com.app.gadjahdjaya

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var receiptAdapter: ReceiptAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var finishButton: Button
    private lateinit var transactionTimeTextView: TextView
    private lateinit var paymentMethodTextView: TextView
    private val receiptList = mutableListOf<MenuItem>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        recyclerView = findViewById(R.id.recyclerView_receipt)
        totalPriceTextView = findViewById(R.id.textview_total_price)
        finishButton = findViewById(R.id.button_finish)
        transactionTimeTextView = findViewById(R.id.textView_transaction_time)
        paymentMethodTextView = findViewById(R.id.textview_metode)

        database = FirebaseDatabase.getInstance().reference.child("transactions")

        val items = intent.getSerializableExtra("receiptList") as List<MenuItem>
        val totalPrice = intent.getIntExtra("totalPrice", 0)
        val paymentMethod = intent.getStringExtra("paymentMethod") ?: "Unknown"

        receiptList.addAll(items)

        receiptAdapter = ReceiptAdapter(this, receiptList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = receiptAdapter

        totalPriceTextView.text = "Rp ${Utils.formatCurrency(totalPrice)}"
        paymentMethodTextView.text = paymentMethod

        val currentTime = getCurrentTime()
        transactionTimeTextView.text = "Transaction Time: $currentTime"

        finishButton.setOnClickListener {
            saveTransactionToFirebase(items, totalPrice, paymentMethod)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun saveTransactionToFirebase(receiptList: List<MenuItem>, totalPrice: Int, paymentMethod: String) {
        val transactionId = database.push().key ?: ""
        val currentTime = getCurrentTime()
        val transaction = Transaction(transactionId, receiptList, totalPrice, currentTime, paymentMethod)
        database.child(transactionId).setValue(transaction)
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    data class Transaction(
        val id: String,
        val items: List<MenuItem>,
        val totalPrice: Int,
        val timestamp: String,
        val paymentMethod: String
    )
}
