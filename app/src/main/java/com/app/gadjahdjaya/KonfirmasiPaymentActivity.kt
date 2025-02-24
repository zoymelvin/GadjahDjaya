package com.app.gadjahdjaya.ui.payment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.model.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class KonfirmasiPaymentActivity : AppCompatActivity() {

    private lateinit var txtTotalPrice: TextView
    private lateinit var inputJumlahUang: EditText
    private lateinit var txtKembalianValue: TextView
    private lateinit var btnUangPas: Button
    private lateinit var btnUangLain1: Button
    private lateinit var btnUangLain2: Button
    private lateinit var btnKonfirmasi: Button

    private var totalHarga: Int = 0
    private var cartList: List<MenuItem> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi_payment)

        // Inisialisasi UI
        txtTotalPrice = findViewById(R.id.txt_total_price)
        inputJumlahUang = findViewById(R.id.input_jumlah_uang)
        txtKembalianValue = findViewById(R.id.txt_kembalian_value)
        btnUangPas = findViewById(R.id.btn_uang_pas)
        btnUangLain1 = findViewById(R.id.btn_uang_lain1)
        btnUangLain2 = findViewById(R.id.btn_uang_lain2)
        btnKonfirmasi = findViewById(R.id.btn_konfirmasi)

        // Ambil data total harga dari intent
        totalHarga = intent.getIntExtra("totalPrice", 0)
        cartList = intent.getParcelableArrayListExtra("cartItems") ?: listOf()

        // Set total harga ke UI dalam format Rupiah
        txtTotalPrice.text = formatCurrency(totalHarga)

        // Set nilai tombol berdasarkan harga
        updateUangLainButtons()

        // Listener jumlah uang
        inputJumlahUang.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hitungKembalian()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tombol uang pas
        btnUangPas.setOnClickListener {
            inputJumlahUang.setText(formatCurrencyPlain(totalHarga))
            hitungKembalian()
        }

        // Tombol uang pecahan pertama
        btnUangLain1.setOnClickListener {
            val uangTerdekat = getUangTerdekat(totalHarga)
            inputJumlahUang.setText(formatCurrencyPlain(uangTerdekat))
            hitungKembalian()
        }

        // Tombol uang pecahan kedua
        btnUangLain2.setOnClickListener {
            val uangLebihTinggi = getUangLebihTinggi(totalHarga)
            inputJumlahUang.setText(formatCurrencyPlain(uangLebihTinggi))
            hitungKembalian()
        }

        // Konfirmasi pembayaran
        btnKonfirmasi.setOnClickListener {
            if (validasiPembayaran()) {
                prosesPembayaranTunai()
            }
        }
    }

    /**
     * ✅ Menghitung kembalian
     */
    private fun hitungKembalian() {
        val jumlahUang = inputJumlahUang.text.toString().replace(".", "").toIntOrNull() ?: 0
        val kembalian = jumlahUang - totalHarga
        txtKembalianValue.text = formatCurrency(if (kembalian >= 0) kembalian else 0)
    }

    /**
     * ✅ Validasi pembayaran sebelum konfirmasi
     */
    private fun validasiPembayaran(): Boolean {
        val jumlahUang = inputJumlahUang.text.toString().replace(".", "").toIntOrNull() ?: 0
        if (jumlahUang < totalHarga) {
            Toast.makeText(this, "Jumlah uang kurang!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * ✅ Menyesuaikan tombol uang terdekat
     */
    private fun updateUangLainButtons() {
        val uangTerdekat = getUangTerdekat(totalHarga)
        val uangLebihTinggi = getUangLebihTinggi(totalHarga)

        btnUangLain1.text = formatCurrency(uangTerdekat)
        btnUangLain2.text = formatCurrency(uangLebihTinggi)
    }

    /**
     * ✅ Mendapatkan pecahan uang terdekat berdasarkan nilai total
     */
    private fun getUangTerdekat(total: Int): Int {
        val pecahan = listOf(5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000,
            55000, 60000, 65000, 70000, 75000, 80000, 85000, 90000, 95000, 100000,
            105000, 110000, 115000, 120000, 125000, 130000, 135000, 140000, 145000, 150000,
            155000, 160000, 165000, 170000, 175000, 180000, 185000, 190000, 195000, 200000)
        return pecahan.firstOrNull { it > total } ?: total
    }

    /**
     * ✅ Mendapatkan pecahan uang lebih tinggi dari uang terdekat
     */
    private fun getUangLebihTinggi(total: Int): Int {
        val pecahan = listOf(5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000, 50000,
            55000, 60000, 65000, 70000, 75000, 80000, 85000, 90000, 95000, 100000,
            105000, 110000, 115000, 120000, 125000, 130000, 135000, 140000, 145000, 150000,
            155000, 160000, 165000, 170000, 175000, 180000, 185000, 190000, 195000, 200000)
        val index = pecahan.indexOfFirst { it > total }
        return if (index != -1 && index + 1 < pecahan.size) pecahan[index + 1] else pecahan.last()
    }

    /**
     * 🚀 **Proses pembayaran tunai & simpan transaksi ke Firebase**
     */
    private fun prosesPembayaranTunai() {
        val transaksiId = "order-${System.currentTimeMillis()}"
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val currentUser = FirebaseAuth.getInstance().currentUser
        val cashierId = currentUser?.uid ?: "unknown"
        val dineOption = intent.getStringExtra("dineOption") ?: "Dine In" // ✅ Ambil data dari intent
        val totalPesanan = intent.getIntExtra("totalPesanan", 0) // ✅ Ambil total pesanan

        Log.d("SwitchDebug", "Menyimpan dineOption ke Firebase: $dineOption") // 🔥 Debugging

        val transaksi = mapOf(
            "order_id" to transaksiId,
            "gross_amount" to totalHarga,
            "timestamp" to timestamp,
            "payment_method" to "cash",
            "status" to "success",
            "cashier_id" to cashierId,  // ✅ Simpan ID kasir
            "dine_option" to dineOption,
            "items" to cartList.map { menuItem ->
                mapOf(
                    "id" to menuItem.id,
                    "name" to menuItem.nama,
                    "price" to menuItem.harga,
                    "quantity" to menuItem.jumlah
                )
            }
        )

        val database = FirebaseDatabase.getInstance().reference.child("transactions/offline")

        database.child(transaksiId).setValue(transaksi).addOnSuccessListener {
            Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
            kurangiStokBahan()  // ✅ Kurangi stok bahan seperti biasa

            if (dineOption == "Take Away") {
                kurangiStokSendokGarpu(totalPesanan) // ✅ Kurangi stok sendok garpu
            }

            pindahKeReceipt(transaksiId, dineOption)  // ✅ Kirim ke ReceiptActivity
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menyimpan transaksi!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * ✅ **Kurangi stok bahan baku "Sendok & Garpu" untuk takeaway**
     */
    private fun kurangiStokSendokGarpu(totalPesanan: Int) {
        val bahanDatabase = FirebaseDatabase.getInstance().reference.child("bahanBaku")

        val sendokGarpuId = "-OJACyMD5f1IXG3C8h86" // ✅ ID dari Firebase

        bahanDatabase.child(sendokGarpuId).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                Log.e("StockUpdate", "❌ Data Sendok & Garpu tidak ditemukan di Firebase!")
                return@addOnSuccessListener
            }

            val stokSaatIni = snapshot.child("stok").getValue(Double::class.java) ?: 0.0
            val stokBaru = (stokSaatIni - totalPesanan).coerceAtLeast(0.0) // Pastikan stok tidak negatif

            Log.d("StockUpdate", "🛠️ Mengurangi stok Sendok & Garpu: $stokSaatIni - $totalPesanan = $stokBaru")

            bahanDatabase.child(sendokGarpuId).child("stok").setValue(stokBaru)
                .addOnSuccessListener {
                    Log.d("StockUpdate", "✅ Stok Sendok & Garpu diperbarui: $stokSaatIni → $stokBaru")
                }
                .addOnFailureListener {
                    Log.e("StockUpdate", "❌ Gagal memperbarui stok Sendok & Garpu!")
                }
        }.addOnFailureListener {
            Log.e("StockUpdate", "❌ Gagal mengambil data stok Sendok & Garpu dari Firebase!")
        }
    }


    /**
     * 🚀 **Kurangi stok bahan baku setelah transaksi sukses**
     */
    private fun kurangiStokBahan() {
        val bahanDatabase = FirebaseDatabase.getInstance().reference.child("bahanBaku")
        val batchUpdates = mutableMapOf<String, Double>()

        for (menuItem in cartList) {
            for (bahan in menuItem.bahanBakuDibutuhkan) {
                if (bahan.idBahan.isBlank()) {
                    Log.e("StockUpdate", "❌ ID bahan kosong untuk ${bahan.namaBahan}, tidak bisa update stok!")
                    continue
                }

                val stokDikurangi = bahan.jumlah * menuItem.jumlah
                batchUpdates[bahan.idBahan] = (batchUpdates[bahan.idBahan] ?: 0.0) - stokDikurangi
            }
        }

        if (batchUpdates.isEmpty()) {
            return
        }

        bahanDatabase.get().addOnSuccessListener { snapshot ->
            val updates = mutableMapOf<String, Any>()
            for ((bahanId, perubahanStok) in batchUpdates) {
                val stokSaatIni = snapshot.child(bahanId).child("stok").getValue(Double::class.java) ?: return@addOnSuccessListener
                updates["$bahanId/stok"] = (stokSaatIni + perubahanStok).coerceAtLeast(0.0)
            }

            if (updates.isNotEmpty()) {
                bahanDatabase.updateChildren(updates)
            }
        }
    }

    private fun pindahKeReceipt(transaksiId: String, dineOption: String) {
        val intent = Intent(this, ReceiptActivity::class.java)
        intent.putExtra("transactionId", transaksiId)
        intent.putExtra("dineOption", dineOption)
        startActivity(intent)
        finish() // Tutup halaman pembayaran
    }

    /**
     * ✅ Format angka ke Rupiah
     */
    private fun formatCurrency(amount: Int): String {
        return DecimalFormat("#,###").format(amount).replace(",", ".")
    }

    /**
     * ✅ Format angka tanpa simbol mata uang (plain)
     */
    private fun formatCurrencyPlain(amount: Int): String {
        return DecimalFormat("#,###").format(amount).replace(",", ".")
    }
}
