package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.databinding.ItemStokMenipisBinding
import com.app.gadjahdjaya.model.BahanBaku

class StokMenipisAdapter(
    private val bahanList: List<BahanBaku>,
    private val onItemClick: (BahanBaku) -> Unit // Callback ketika item diklik
) : RecyclerView.Adapter<StokMenipisAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemStokMenipisBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bahan: BahanBaku) {
            binding.tvNamaBahan.text = bahan.nama

            // **✅ Perbaikan: Format stok agar tidak menggunakan desimal jika bilangan bulat**
            val stokFormatted = if (bahan.stok % 1 == 0.0) {
                bahan.stok.toInt().toString() // Hapus ".0" jika bilangan bulat
            } else {
                bahan.stok.toString() // Jika ada desimal, tetap tampilkan
            }

            binding.tvStokDetail.text = "Stok: $stokFormatted ${bahan.satuan}"

            // **🔹 Menampilkan progress stok dalam bentuk persentase**
            val stokPersentase = ((bahan.stok / bahan.stokMaksimum) * 100).toInt()
            binding.progressStok.progress = stokPersentase

            // **🔹 Warna progress berdasarkan level stok**
            when {
                stokPersentase < 25 -> binding.progressStok.progressDrawable.setTint(android.graphics.Color.RED)
                stokPersentase in 25..50 -> binding.progressStok.progressDrawable.setTint(android.graphics.Color.YELLOW)
                else -> binding.progressStok.progressDrawable.setTint(android.graphics.Color.GREEN)
            }

            // **🔹 Aksi klik untuk menampilkan detail stok**
            binding.root.setOnClickListener {
                onItemClick(bahan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStokMenipisBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bahanList[position])
    }

    override fun getItemCount(): Int = bahanList.size
}
