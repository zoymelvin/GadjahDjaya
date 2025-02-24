package com.app.gadjahdjaya

import android.content.Context
import com.app.gadjahdjaya.model.MenuItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class MenuAdapter(
    private val context: Context,
    private var menuList: List<MenuItem>,
    private val itemClickListener: OnItemClickListener?
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(menuItem: MenuItem)
        fun onDeleteClick(menuItem: MenuItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageview_menu)
        val titleView: TextView = itemView.findViewById(R.id.tampilnamamenu)
        val priceView: TextView = itemView.findViewById(R.id.tampilhargamenu)
        val editButton: ImageButton = itemView.findViewById(R.id.ic_edit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.ic_delete)

        fun bind(menuItem: MenuItem) {
            titleView.text = menuItem.nama
            priceView.text = "Rp ${formatRupiah(menuItem.harga)}"
            Glide.with(context).load(menuItem.gambar).into(imageView)

            editButton.setOnClickListener {
                itemClickListener?.onEditClick(menuItem)
            }

            deleteButton.setOnClickListener {
                itemClickListener?.onDeleteClick(menuItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_menu, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int = menuList.size

    fun updateList(newMenuList: List<MenuItem>) {
        menuList = newMenuList
        notifyDataSetChanged()
    }

    private fun formatRupiah(amount: Int): String {
        val symbols = DecimalFormatSymbols()
        symbols.groupingSeparator = '.'
        val formatter = DecimalFormat("#,###", symbols)
        return formatter.format(amount)
    }
}
