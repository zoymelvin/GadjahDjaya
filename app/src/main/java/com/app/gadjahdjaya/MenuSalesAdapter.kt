package com.app.gadjahdjaya

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

// Data class untuk menyimpan informasi menu
data class MenuSalesData(
    val id: String,
    val menuName: String,
    val quantity: Int,
    val revenue: Int,
    val imageUrl: String,
    val date: String
)

class MenuSalesAdapter(private var menuList: List<MenuSalesData>) :
    RecyclerView.Adapter<MenuSalesAdapter.MenuViewHolder>() {

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMenu: ImageView = view.findViewById(R.id.imgMenu)
        val tvMenuName: TextView = view.findViewById(R.id.tvMenuName)
        val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
        val tvRevenue: TextView = view.findViewById(R.id.tvRevenue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_sales, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = menuList[position]
        Glide.with(holder.itemView.context).load(item.imageUrl).into(holder.imgMenu)
        holder.tvMenuName.text = item.menuName
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvRevenue.text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(item.revenue)}"
    }

    override fun getItemCount(): Int = menuList.size

    fun updateList(newList: List<MenuSalesData>) {
        menuList = newList
        notifyDataSetChanged()
    }
}
