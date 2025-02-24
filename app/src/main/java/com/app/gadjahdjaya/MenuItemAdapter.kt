package com.app.gadjahdjaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.app.gadjahdjaya.model.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MenuItemAdapter(private val menuItems: List<MenuItem>) : RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu_detail, parent, false)
        return MenuItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)
    }

    override fun getItemCount(): Int = menuItems.size

    class MenuItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewMenu: ImageView = itemView.findViewById(R.id.imageView_menu)
        private val textViewName: TextView = itemView.findViewById(R.id.textView_name)
        private val textViewQuantity: TextView = itemView.findViewById(R.id.textView_quantity)
        private val textViewPrice: TextView = itemView.findViewById(R.id.textView_price)

        fun bind(menuItem: MenuItem) {
            Picasso.get().load(menuItem.gambar).into(imageViewMenu)
            textViewName.text = menuItem.nama
            textViewQuantity.text = "X ${menuItem.jumlah}"
            textViewPrice.text = "Rp ${Utils.formatCurrency(menuItem.harga * menuItem.jumlah)}"
        }
    }
}
