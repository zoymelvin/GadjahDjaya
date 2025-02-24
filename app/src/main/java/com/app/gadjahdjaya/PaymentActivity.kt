package com.app.gadjahdjaya.ui.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.MenuPaymentAdapter
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.model.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class PaymentActivity : AppCompatActivity(), MenuPaymentAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MenuPaymentAdapter
    private lateinit var totalPriceTextView: TextView
    private lateinit var btnTunai: Button
    private lateinit var btnOnline: Button
    private lateinit var switchTakeaway: Switch

    private val cartList: MutableList<MenuItem> = mutableListOf()
    private var totalPrice: Int = 0
    private var dineOption: String = "Dine In"
    private val client = OkHttpClient()

    private var customerId: String? = null
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // 🔹 Ambil ID pengguna yang sedang login dari FirebaseAuth
        val auth = FirebaseAuth.getInstance()
        customerId = auth.currentUser?.uid

        if (customerId == null) {
            Toast.makeText(this, "Gagal mendapatkan ID pengguna", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().reference

        // Mengambil data pesanan dari intent
        val cartItems: ArrayList<MenuItem>? = intent.getParcelableArrayListExtra("cartItems")
        cartItems?.let { cartList.addAll(it) }

        recyclerView = findViewById(R.id.recyclerView_cart)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MenuPaymentAdapter(this, cartList, this)
        recyclerView.adapter = adapter

        totalPriceTextView = findViewById(R.id.textview_total_price)
        btnTunai = findViewById(R.id.btn_payment_cash)
        btnOnline = findViewById(R.id.btn_payment_online)
        switchTakeaway = findViewById(R.id.switch_takeaway)

        updateTotalPrice()

        // ✅ Periksa perubahan di switch Takeaway
        switchTakeaway.setOnCheckedChangeListener { _, isChecked ->
            dineOption = if (isChecked) "Take Away" else "Dine In"
            Log.d("SwitchDebug", "Pilihan: $dineOption")
        }

        // **Navigasi ke Halaman Konfirmasi Pembayaran untuk Tunai**
        btnTunai.setOnClickListener {
            navigateToConfirmPayment()
        }

        // **Navigasi ke Pembayaran Online**
        btnOnline.setOnClickListener {
            initiateOnlinePayment()
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
        totalPriceTextView.text = "Total: Rp $totalPrice"
    }

    private fun navigateToConfirmPayment() {
        val intent = Intent(this, KonfirmasiPaymentActivity::class.java)
        intent.putExtra("totalPrice", totalPrice)
        intent.putExtra("dineOption", dineOption)
        intent.putParcelableArrayListExtra("cartItems", ArrayList(cartList))
        startActivity(intent)
    }

    private fun initiateOnlinePayment() {
        val orderId = "ORDER-${System.currentTimeMillis()}"

        val items = cartList.map {
            mapOf(
                "id" to it.id,
                "price" to it.harga,
                "quantity" to it.jumlah,
                "name" to it.nama
            )
        }

        val paymentData = mapOf(
            "orderId" to orderId,
            "customerId" to customerId,
            "items" to items,
            "dineOption" to dineOption
        )

        val jsonData = Gson().toJson(paymentData)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonData)

        val request = Request.Builder()
            .url("https://us-central1-gadjahdjaya-78fdf.cloudfunctions.net/api/getSnapToken")
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@PaymentActivity, "Terjadi kesalahan saat memproses pembayaran", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()

                if (!response.isSuccessful || responseData.isNullOrEmpty()) {
                    runOnUiThread {
                        Toast.makeText(this@PaymentActivity, "Gagal mendapatkan token pembayaran", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val type = object : TypeToken<MidtransResponse>() {}.type
                val midtransResponse: MidtransResponse = Gson().fromJson(responseData, type)

                runOnUiThread {
                    if (!midtransResponse.token.isNullOrEmpty() && !midtransResponse.redirectUrl.isNullOrEmpty()) {
                        val intent = Intent(this@PaymentActivity, MidtransPaymentActivity::class.java)
                        intent.putExtra("redirectUrl", midtransResponse.redirectUrl)
                        intent.putExtra("orderId", orderId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@PaymentActivity, "Gagal mendapatkan token pembayaran", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    data class MidtransResponse(val token: String?, val redirectUrl: String?)
}
