package com.app.gadjahdjaya.ui.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.gadjahdjaya.R
import com.google.firebase.database.*

class MidtransPaymentActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnCheckStatus: Button
    private lateinit var database: DatabaseReference
    private var orderId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_midtrans_payment)

        webView = findViewById(R.id.webView)
        btnCheckStatus = findViewById(R.id.btn_check_status)
        database = FirebaseDatabase.getInstance().reference

        // ✅ Ambil Order ID dari Intent, cek agar tidak null
        orderId = intent.getStringExtra("orderId") ?: ""

        if (orderId.isEmpty()) {
            Toast.makeText(this, "Error: Order ID tidak ditemukan!", Toast.LENGTH_SHORT).show()
            Log.e("MidtransPayment", "Order ID tidak ada di Intent!")
            finish()
            return
        }

        val redirectUrl = intent.getStringExtra("redirectUrl") ?: ""

        if (redirectUrl.isNotEmpty()) {
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("WebView", "Loaded: $url")
                }
            }
            webView.loadUrl(redirectUrl)
        } else {
            Toast.makeText(this, "URL Pembayaran tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        // ✅ Saat tombol "Check Status" ditekan, periksa status transaksi
        btnCheckStatus.setOnClickListener {
            checkTransactionStatus()
        }
    }

    /**
     * ✅ **Cek status transaksi di Firebase setelah pembayaran selesai**
     */
    private fun checkTransactionStatus() {
        val transactionRef = database.child("transactions").child("online")

        transactionRef.orderByChild("order_id").startAt(orderId!!).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val status = childSnapshot.child("status").getValue(String::class.java) ?: "pending"
                            val matchedOrderId = childSnapshot.key ?: ""
                            Log.d("CheckStatus", "Order ditemukan: $matchedOrderId dengan status: $status")

                            if (status == "settlement") {
                                redirectToReceipt(matchedOrderId)
                            } else {
                                Toast.makeText(
                                    this@MidtransPaymentActivity,
                                    "Pembayaran belum selesai. Coba lagi nanti!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return
                        }
                    } else {
                        Toast.makeText(
                            this@MidtransPaymentActivity,
                            "Transaksi tidak ditemukan di Firebase!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("CheckStatus", "Transaksi tidak ditemukan untuk Order ID: $orderId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Gagal mengambil status transaksi: ${error.message}")
                }
            })
    }



    /**
     * ✅ **Pindah ke ReceiptActivity setelah transaksi sukses**
     */
    private fun redirectToReceipt(transactionKey: String) {
        Log.d("Redirect", "Pindah ke ReceiptActivity dengan Order ID: $transactionKey")

        val intent = Intent(this, ReceiptActivity::class.java)
        intent.putExtra("transactionId", transactionKey)
        startActivity(intent)
        finish()
    }
}
