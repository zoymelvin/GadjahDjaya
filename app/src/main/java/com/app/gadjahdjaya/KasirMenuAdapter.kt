package com.app.gadjahdjaya

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import android.widget.TextView
import android.widget.ImageButton

class KasirMenuAdapter(
    private val context: Context,
    private var menuList: List<MenuItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<KasirMenuAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onIncreaseQuantity(menuItem: MenuItem)
        fun onDecreaseQuantity(menuItem: MenuItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.imageview_menu)
        val titleView: TextView = itemView.findViewById(R.id.tampilnamamenu)
        val priceView: TextView = itemView.findViewById(R.id.tampilhargamenu)
        val addButton: ImageButton = itemView.findViewById(R.id.ic_plus)
        val minusButton: ImageButton = itemView.findViewById(R.id.ic_minus)
        val quantityView: TextView = itemView.findViewById(R.id.jumlahmenu)

        fun bind(menuItem: MenuItem, clickListener: OnItemClickListener) {
            Glide.with(context)
                .load(menuItem.gambar)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(imageView)

            titleView.text = menuItem.nama
            priceView.text = Utils.formatCurrency(menuItem.harga)
            quantityView.text = menuItem.jumlah.toString()

            addButton.setOnClickListener {
                clickListener.onIncreaseQuantity(menuItem)
                quantityView.text = menuItem.jumlah.toString()
            }

            minusButton.setOnClickListener {
                clickListener.onDecreaseQuantity(menuItem)
                quantityView.text = menuItem.jumlah.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_menu_kasir, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuList[position]
        holder.bind(menuItem, itemClickListener)
    }

    override fun getItemCount(): Int = menuList.size

    fun updateData(newMenuList: List<MenuItem>) {
        menuList = newMenuList
        notifyDataSetChanged()
    }
}
