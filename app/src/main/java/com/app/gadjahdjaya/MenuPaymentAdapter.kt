package com.app.gadjahdjaya

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.model.MenuItem
import com.squareup.picasso.Picasso

class MenuPaymentAdapter(
    private val context: Context,
    private val menuList: List<MenuItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MenuPaymentAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onIncreaseQuantity(menuItem: MenuItem)
        fun onDecreaseQuantity(menuItem: MenuItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_menu_payment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuList[position]

        holder.menuName.text = menuItem.nama
        holder.menuQuantity.text = "X ${menuItem.jumlah}"
        holder.menuPrice.text = "Rp ${Utils.formatCurrency(menuItem.harga * menuItem.jumlah)}"

        // Load the image using Picasso
        if (menuItem.gambar.startsWith("http")) {
            Picasso.get().load(menuItem.gambar).into(holder.menuImage)
        } else {
            val resId = context.resources.getIdentifier(menuItem.gambar, "drawable", context.packageName)
            holder.menuImage.setImageResource(resId)
        }

        holder.increaseButton.setOnClickListener {
            listener.onIncreaseQuantity(menuItem)
        }

        holder.decreaseButton.setOnClickListener {
            listener.onDecreaseQuantity(menuItem)
        }
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val menuImage: ImageView = itemView.findViewById(R.id.imageview_menu)
        val menuName: TextView = itemView.findViewById(R.id.textview_menu_name)
        val menuQuantity: TextView = itemView.findViewById(R.id.textview_menu_quantity)
        val menuPrice: TextView = itemView.findViewById(R.id.textview_total_price)
        val increaseButton: ImageView = itemView.findViewById(R.id.imageview_increase)
        val decreaseButton: ImageView = itemView.findViewById(R.id.imageview_decrease)
    }
}
