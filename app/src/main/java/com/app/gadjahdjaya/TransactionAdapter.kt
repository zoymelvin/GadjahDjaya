package com.app.gadjahdjaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val items: List<Any>,
    private val itemClickListener: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HORIZONTAL = 0
        private const val VIEW_TYPE_TRANSACTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Transaction -> VIEW_TYPE_TRANSACTION
            else -> throw IllegalArgumentException("Invalid type of data")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HORIZONTAL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_horizontal_card_list, parent, false)
                HorizontalViewHolder(view)
            }
            VIEW_TYPE_TRANSACTION -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
                TransactionViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> holder.bind(items[position] as Transaction, itemClickListener, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class HorizontalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerViewHorizontal: RecyclerView = itemView.findViewById(R.id.recyclerView_horizontal)
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewMenu: ImageView = itemView.findViewById(R.id.imageView_menu)
        private val textViewTransaction: TextView = itemView.findViewById(R.id.textView_transaction)
        private val textViewTotalPrice: TextView = itemView.findViewById(R.id.textView_totalPrice)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textView_timestamp)

        fun bind(transaction: Transaction, clickListener: (Transaction) -> Unit, position: Int) {
            val menuItem = transaction.items.firstOrNull()

            // Validasi sebelum memuat gambar
            if (menuItem != null && !menuItem.gambar.isNullOrEmpty()) {
                Picasso.get()
                    .load(menuItem.gambar)
                    .placeholder(R.drawable.placeholder_image) // Gambar default jika belum tersedia
                    .into(imageViewMenu)
            } else {
                // Gunakan gambar placeholder jika path gambar null/kosong
                imageViewMenu.setImageResource(R.drawable.placeholder_image)
            }

            textViewTransaction.text = "Transaksi ${position + 1}"
            textViewTotalPrice.text = "+ Rp ${Utils.formatCurrency(transaction.totalPrice)}"
            textViewTimestamp.text = formatTimestamp(transaction.timestamp)

            itemView.setOnClickListener {
                clickListener(transaction)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = format.parse(timestamp)
                val newFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                newFormat.format(date)
            } catch (e: Exception) {
                "Invalid Date"
            }
        }
    }

}
