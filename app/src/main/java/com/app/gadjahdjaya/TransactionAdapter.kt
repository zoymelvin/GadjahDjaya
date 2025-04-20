package com.app.gadjahdjaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val transactions: List<Transaksi>,
    private val itemClickListener: (Transaksi) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var expandedTransactionId: String? = null // Menyimpan transaksi yang sedang diperluas

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction, itemClickListener)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDetailPesananLabel: TextView = itemView.findViewById(R.id.tvDetailPesananLabel)
        private val detailPesananLayout: LinearLayout = itemView.findViewById(R.id.detailPesananLayout)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription) // 🔥 Tambahkan ini

        fun bind(transaction: Transaksi, clickListener: (Transaksi) -> Unit) {

            val formattedAmount = formatCurrency(transaction.amount)
            tvAmount.text = "Rp $formattedAmount"

            tvDate.text = formatTimestamp(transaction.timestamp)
            tvCategory.text = if (transaction.amount >= 0) "Pemasukan" else "Pengeluaran"

            // Set warna berdasarkan tipe transaksi
            val color = if (transaction.amount >= 0) R.color.green else R.color.red
            tvAmount.setTextColor(itemView.context.getColor(color))
            tvCategory.setTextColor(itemView.context.getColor(color))

            // Mengecek apakah transaksi ini sedang diperluas
            val isExpanded = transaction.id == expandedTransactionId

            // 🔥 Perbaiki: Sembunyikan detail pesanan & deskripsi pengeluaran secara default
            detailPesananLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            tvDescription.visibility = if (isExpanded && transaction.amount < 0) View.VISIBLE else View.GONE

            // 🔥 Jika transaksi adalah pengeluaran, tampilkan deskripsi
            if (transaction.amount < 0) {
                tvDescription.text = "Deskripsi: ${transaction.description}"
            }

            // 🔥 Pastikan tidak ada duplikasi data sebelum menambahkan item baru
            if (isExpanded) {
                detailPesananLayout.removeAllViews()

                if (transaction.items.isNotEmpty()) {
                    tvDetailPesananLabel.visibility = View.VISIBLE

                    for (item in transaction.items) {
                        val itemView = LayoutInflater.from(itemView.context)
                            .inflate(R.layout.item_detail_pesanan, detailPesananLayout, false)

                        val tvItemName = itemView.findViewById<TextView>(R.id.tvItemName)
                        val tvItemPrice = itemView.findViewById<TextView>(R.id.tvItemPrice)

                        tvItemName.text = "${item.name} x${item.quantity}"
                        tvItemPrice.text = "Rp ${formatCurrency(item.price)}"

                        detailPesananLayout.addView(itemView)
                    }

                } else {
                    tvDetailPesananLabel.visibility = View.GONE
                }
            }

            // Expand/collapse saat item diklik
            itemView.setOnClickListener {
                expandedTransactionId = if (isExpanded) null else transaction.id
                notifyDataSetChanged() // Update tampilan secara langsung
                clickListener(transaction)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = format.parse(timestamp)
                val newFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                newFormat.format(date!!)
            } catch (e: Exception) {
                "Invalid Date"
            }
        }

        private fun formatCurrency(amount: Double): String {
            return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
        }
    }
}
