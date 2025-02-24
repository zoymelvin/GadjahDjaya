package com.app.gadjahdjaya.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.model.MenuItem
import java.text.NumberFormat
import java.util.*

class ReceiptAdapter(
    private val context: Context,
    private val receiptList: List<MenuItem>
) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_receipt, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = receiptList[position]

        holder.menuName.text = menuItem.nama
        holder.menuPrice.text = formatCurrency(menuItem.harga)
        holder.menuQuantity.text = menuItem.jumlah.toString()
        holder.menuTotal.text = formatCurrency(menuItem.harga * menuItem.jumlah)
    }

    override fun getItemCount(): Int {
        return receiptList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuName: TextView = itemView.findViewById(R.id.tv_item_name)
        val menuPrice: TextView = itemView.findViewById(R.id.tv_item_price)
        val menuQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity)
        val menuTotal: TextView = itemView.findViewById(R.id.tv_item_total)
    }

    /**
     * ✅ **Format angka ke Rupiah**
     */
    private fun formatCurrency(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ")
    }
}
