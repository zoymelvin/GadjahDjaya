package com.app.gadjahdjaya

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class PaymentActivity : AppCompatActivity(), MenuPaymentAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MenuPaymentAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var proceedButton: Button
    private lateinit var qrisButton: Button
    private val cartList: MutableList<MenuItem> = mutableListOf()
    private var totalPrice: Int = 0
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Get the cart items from the intent
        val cartItems: ArrayList<MenuItem>? = intent.getParcelableArrayListExtra("cartItems")
        if (cartItems != null) {
            cartList.addAll(cartItems)
        }

        recyclerView = findViewById(R.id.recyclerView_cart)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MenuPaymentAdapter(this, cartList, this)
        recyclerView.adapter = adapter

        totalPriceTextView = findViewById(R.id.textview_total_price)
        proceedButton = findViewById(R.id.btn_payment_cash)
        qrisButton = findViewById(R.id.btn_qris)
        database = FirebaseDatabase.getInstance().reference

        updateTotalPrice()

        proceedButton.setOnClickListener {
            proceedToReceipt("Cash")
        }

        qrisButton.setOnClickListener {
            showQrisPopup()
        }
    }

    override fun onIncreaseQuantity(menuItem: MenuItem) {
        menuItem.jumlah++
        updateTotalPrice()
        adapter.notifyDataSetChanged()
    }

    override fun onDecreaseQuantity(menuItem: MenuItem) {
        if (menuItem.jumlah > 1) {
            menuItem.jumlah--
        } else {
            cartList.remove(menuItem)
        }
        updateTotalPrice()
        adapter.notifyDataSetChanged()
    }

    private fun updateTotalPrice() {
        totalPrice = cartList.sumOf { it.harga * it.jumlah }
        totalPriceTextView.text = "Total Price: Rp ${Utils.formatCurrency(totalPrice)}"
    }

    private fun proceedToReceipt(paymentMethod: String) {
        val intent = Intent(this, ReceiptActivity::class.java)
        intent.putParcelableArrayListExtra("receiptList", ArrayList(cartList))
        intent.putExtra("totalPrice", totalPrice)
        intent.putExtra("paymentMethod", paymentMethod)
        startActivity(intent)
    }

    private fun showQrisPopup() {
        // Create a dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_qris)
        dialog.setCancelable(true)

        // Get the QRIS image view and finish button from the dialog layout
        val qrisImageView = dialog.findViewById<ImageView>(R.id.qris_image_view)
        val finishButton = dialog.findViewById<Button>(R.id.btn_finish)

        // Fetch the QRIS code URL from Firebase Realtime Database
        database.child("QRISCODE").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val qrisMap = dataSnapshot.value as Map<*, *>
                    val qrisCodeUrl = qrisMap.values.firstOrNull() as? String
                    if (!qrisCodeUrl.isNullOrEmpty()) {
                        // Load the QRIS code image using Picasso
                        Picasso.get().load(qrisCodeUrl).placeholder(R.drawable.placeholder_image).into(qrisImageView)
                    } else {
                        // Handle the case where the URL is empty or null
                        qrisImageView.setImageResource(R.drawable.placeholder_image)
                    }
                } else {
                    // Handle the case where the data snapshot does not exist
                    qrisImageView.setImageResource(R.drawable.placeholder_image)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors
                Toast.makeText(this@PaymentActivity, "Failed to load QR code", Toast.LENGTH_SHORT).show()
                qrisImageView.setImageResource(R.drawable.placeholder_image)
            }
        })

        finishButton.setOnClickListener {
            dialog.dismiss()
            proceedToReceipt("QRIS")
        }

        dialog.show()
    }
}
