package com.app.gadjahdjaya

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class MenuAdapterBeranda(
    private val context: Context,
    private val menuList: List<MenuItem>,
    private val itemClickListener: OnItemClickListener?
) : RecyclerView.Adapter<MenuAdapterBeranda.ViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(menuItem: MenuItem)
        fun onDeleteClick(menuItem: MenuItem)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView? = itemView.findViewById(R.id.imageview_menu)
        val titleView: TextView? = itemView.findViewById(R.id.tampilnamamenu)
        val priceView: TextView? = itemView.findViewById(R.id.tampilhargamenu)
        val editButton: ImageButton? = itemView.findViewById(R.id.ic_edit)
        val deleteButton: ImageButton? = itemView.findViewById(R.id.ic_delete)

        init {
            editButton?.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.onEditClick(menuList[position])
                }
            }

            deleteButton?.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener?.onDeleteClick(menuList[position])
                }
            }
        }

        fun bind(menuItem: MenuItem) {
            titleView?.text = menuItem.nama
            priceView?.text = "Rp. ${menuItem.harga}"
            Glide.with(context).load(menuItem.gambar).into(imageView!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.list_menu_beranda, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int = menuList.size
}
