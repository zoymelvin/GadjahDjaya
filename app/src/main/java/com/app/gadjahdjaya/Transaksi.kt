package com.app.gadjahdjaya

data class Transaksi(
    val id: String = "",
    val amount: Double = 0.0,
    val timestamp: String = "",
    val status: String = "",
    val payment_method: String = "",
    val items: List<Item> = emptyList(),
    val description: String = "",
    val gross_amount: Double = 0.0
)
