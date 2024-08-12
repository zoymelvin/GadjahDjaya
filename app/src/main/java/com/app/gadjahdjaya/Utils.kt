package com.app.gadjahdjaya

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object Utils {
    fun formatCurrency(amount: Int): String {
        val format: NumberFormat = DecimalFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "").replace(",00", "").trim()
    }
}
