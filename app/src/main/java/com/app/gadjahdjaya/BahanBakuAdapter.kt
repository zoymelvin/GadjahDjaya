package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.databinding.ItemStokBahanBinding
import com.app.gadjahdjaya.model.BahanBaku

class BahanBakuAdapter(
    private val bahanList: List<BahanBaku>,
    private val onItemClick: (BahanBaku) -> Unit // Callback untuk item klik
) : RecyclerView.Adapter<BahanBakuAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemStokBahanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bahan: BahanBaku) {
            binding.tvNamaBahan.text = bahan.nama
            binding.tvKategoriBahan.text = "Kategori: ${bahan.kategori}"

            // **✅ Perbaikan Format Stok agar tidak desimal jika angkanya bulat**
            val stokFormatted = if (bahan.stok % 1 == 0.0) {
                bahan.stok.toInt().toString() // Jika bilangan bulat, hapus ".0"
            } else {
                bahan.stok.toString() // Jika ada desimal, tetap tampilkan
            }

            binding.tvStokBahan.text = "Stok: $stokFormatted ${bahan.satuan}"

            // Aksi klik untuk menampilkan detail bahan
            binding.root.setOnClickListener {
                onItemClick(bahan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStokBahanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bahanList[position])
    }

    override fun getItemCount(): Int = bahanList.size
}
