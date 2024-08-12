package com.app.gadjahdjaya

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
        holder.menuQuantity.text = "X ${menuItem.jumlah}"
        holder.menuPrice.text = "Rp ${Utils.formatCurrency(menuItem.harga * menuItem.jumlah)}"
    }

    override fun getItemCount(): Int {
        return receiptList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuName: TextView = itemView.findViewById(R.id.textview_menu_name)
        val menuQuantity: TextView = itemView.findViewById(R.id.textview_menu_quantity)
        val menuPrice: TextView = itemView.findViewById(R.id.textview_menu_price)
    }
}
