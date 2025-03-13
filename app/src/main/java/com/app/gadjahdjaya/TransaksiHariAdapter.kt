package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.model.Transaksi
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class TransaksiHariAdapter(private val transaksiList: List<Transaksi>) :
    RecyclerView.Adapter<TransaksiHariAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tv_id_transaksi)
        val tvTimestamp: TextView = view.findViewById(R.id.tv_tgltransaksi)
        val tvTotalHarga: TextView = view.findViewById(R.id.tv_total_harga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaksi_hari, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaksi = transaksiList[position]

        // Ambil bagian kedua dari order_id sebelum tanda "-" kedua, dan ubah ke huruf kapital
        val orderParts = transaksi.orderId.split("-")
        val shortOrderId = if (orderParts.size > 2) "${orderParts[0]}-${orderParts[1]}" else transaksi.orderId
        holder.tvOrderId.text = shortOrderId.uppercase(Locale.getDefault())
        holder.tvTimestamp.text = transaksi.timestamp

        // Format harga dengan tanda titik untuk ribuan
        val symbols = DecimalFormatSymbols(Locale("id", "ID"))
        symbols.groupingSeparator = '.'
        val formatter = DecimalFormat("#,###", symbols)
        holder.tvTotalHarga.text = "Rp ${formatter.format(transaksi.grossAmount)}"
    }

    override fun getItemCount(): Int = transaksiList.size
}
