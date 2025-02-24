package com.app.gadjahdjaya

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    // Format angka menjadi mata uang IDR
    fun formatCurrency(amount: Int): String {
        val format: NumberFormat = DecimalFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "").replace(",00", "").trim()
    }

    // Fungsi untuk mendapatkan timestamp saat ini
    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
