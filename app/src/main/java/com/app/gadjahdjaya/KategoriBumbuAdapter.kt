package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.databinding.ItemKategoriBumbuBinding
import com.app.gadjahdjaya.model.BahanBaku

class KategoriBumbuAdapter(
    private val kategoriMap: Map<String, List<BahanBaku>>,
    private val onKategoriClick: (String, List<BahanBaku>) -> Unit
) : RecyclerView.Adapter<KategoriBumbuAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemKategoriBumbuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(kategori: String, bahanBakuList: List<BahanBaku>) {
            val totalPersen = if (bahanBakuList.isNotEmpty()) {
                bahanBakuList.sumOf {
                    (it.stok / it.stokMaksimum) * 100
                } / bahanBakuList.size
            } else {
                0.0
            }

            binding.tvKategoriNama.text = kategori.capitalize()
            binding.progressKategori.progress = totalPersen.toInt()
            binding.tvKategoriPersentase.text = "${totalPersen.toInt()}%"

            binding.root.setOnClickListener {
                onKategoriClick(kategori, bahanBakuList)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKategoriBumbuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kategori = kategoriMap.keys.elementAt(position)
        val bahanBakuList = kategoriMap[kategori] ?: emptyList()
        holder.bind(kategori, bahanBakuList)
    }

    override fun getItemCount(): Int = kategoriMap.size
}