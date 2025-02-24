package com.app.gadjahdjaya.ui.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.HomeActivity
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.adapter.ReceiptAdapter
import com.app.gadjahdjaya.model.MenuItem
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReceiptActivity : AppCompatActivity() {

    private lateinit var tvTransactionId: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvCashier: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var btnSelesai: Button

    private lateinit var database: DatabaseReference
    private lateinit var receiptAdapter: ReceiptAdapter
    private val receiptList: MutableList<MenuItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        // 🔹 Inisialisasi Views
        tvTransactionId = findViewById(R.id.tv_transaction_id)
        tvDate = findViewById(R.id.tv_date)
        tvCashier = findViewById(R.id.tv_cashier)
        tvPaymentMethod = findViewById(R.id.tv_payment_method)
        tvStatus = findViewById(R.id.tv_status)
        tvSubtotal = findViewById(R.id.tv_subtotal)
        tvTotal = findViewById(R.id.tv_total)
        rvItems = findViewById(R.id.rv_items)
        btnSelesai = findViewById(R.id.btn_selesai)

        rvItems.layoutManager = LinearLayoutManager(this)
        receiptAdapter = ReceiptAdapter(this, receiptList)
        rvItems.adapter = receiptAdapter

        // 🔹 Mendapatkan Transaction ID dari Intent
        val transactionId = intent.getStringExtra("transactionId")
        if (!transactionId.isNullOrEmpty()) {
            loadTransactionDetails(transactionId)
        } else {
            Toast.makeText(this, "Transaction ID tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // 🔹 Tombol "Selesai" akan kembali ke halaman utama (Home)
        btnSelesai.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * ✅ **Memuat detail transaksi dari Firebase (`online` & `offline`)**
     */
    private fun loadTransactionDetails(transactionId: String) {
        database = FirebaseDatabase.getInstance().reference

        // 🔹 Periksa transaksi di `online`
        database.child("transactions").child("online").child(transactionId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        processTransactionData(snapshot) // ✅ Jika ditemukan, proses data
                    } else {
                        // 🔹 Jika transaksi tidak ditemukan di `online`, periksa di `offline`
                        database.child("transactions").child("offline").child(transactionId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshotOffline: DataSnapshot) {
                                    if (snapshotOffline.exists()) {
                                        processTransactionData(snapshotOffline) // ✅ Jika ditemukan di `offline`, proses data
                                    } else {
                                        // 🔹 Jika transaksi tidak ditemukan di mana pun, tampilkan pesan error
                                        Toast.makeText(this@ReceiptActivity, "Transaksi tidak ditemukan!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@ReceiptActivity, "Gagal memuat transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ReceiptActivity, "Gagal memuat transaksi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * ✅ **Memproses data transaksi dan menampilkan di UI**
     */
    private fun processTransactionData(snapshot: DataSnapshot) {
        val transaction = snapshot.value as? Map<*, *>

        if (transaction == null) {
            Toast.makeText(this@ReceiptActivity, "Data transaksi tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvTransactionId.text = "Transaction ID: ${transaction["order_id"] ?: "N/A"}"
        tvDate.text = "Date: ${formatDate(transaction["timestamp"]?.toString() ?: "")}"
        tvPaymentMethod.text = "Payment: ${transaction["payment_method"] ?: "N/A"}"
        tvStatus.text = "Status: ${transaction["status"] ?: "N/A"}"

        val dineOption = transaction["dine_option"]?.toString() ?: "Dine In"
        findViewById<TextView>(R.id.tv_dinein_takeaway).text = dineOption

        val cashierId = transaction["cashier_id"]?.toString()
        if (!cashierId.isNullOrEmpty()) {
            tvCashier.text = "Cashier: $cashierId"  // 🔹 Langsung tampilkan jika nama kasir sudah tersedia
        } else {
            tvCashier.text = "Cashier: Unknown"
        }

        // 🔹 Menampilkan harga dengan format Rupiah
        val subtotal = transaction["gross_amount"]?.toString()?.toIntOrNull() ?: 0
        tvSubtotal.text = formatCurrency(subtotal)
        tvTotal.text = formatCurrency(subtotal)

        // 🔹 Mengambil daftar item
        val items = transaction["items"] as? List<Map<*, *>>
        receiptList.clear()
        items?.forEach {
            receiptList.add(
                MenuItem(
                    id = it["id"]?.toString() ?: UUID.randomUUID().toString(),
                    nama = it["name"]?.toString() ?: "Unknown Item",
                    harga = it["price"]?.toString()?.toIntOrNull() ?: 0,
                    jumlah = it["quantity"]?.toString()?.toIntOrNull() ?: 0
                )
            )
        }
        receiptAdapter.notifyDataSetChanged()
    }

    /**
     * ✅ **Memformat tanggal dari timestamp**
     */
    private fun formatDate(timestamp: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(timestamp)
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date ?: Date())
        } catch (e: Exception) {
            "N/A"
        }
    }

    /**
     * ✅ **Format angka ke Rupiah**
     */
    private fun formatCurrency(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }
}
