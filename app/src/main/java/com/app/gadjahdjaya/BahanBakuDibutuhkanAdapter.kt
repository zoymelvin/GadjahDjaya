package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.databinding.ItemBahanBakuDibutuhkanBinding
import com.app.gadjahdjaya.model.BahanBakuDibutuhkan

class BahanBakuDibutuhkanAdapter(
    private val bahanList: MutableList<BahanBakuDibutuhkan>,
    private val onRemoveClick: (BahanBakuDibutuhkan) -> Unit
) : RecyclerView.Adapter<BahanBakuDibutuhkanAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBahanBakuDibutuhkanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bahan: BahanBakuDibutuhkan) {
            binding.tvNamaBahan.text = bahan.namaBahan
            binding.tvJumlahBahan.text = "${bahan.jumlah} ${bahan.satuan}"

            // ✅ Tombol "Hapus" untuk menghapus bahan dari daftar
            binding.btnHapus.setOnClickListener {
                onRemoveClick(bahan)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBahanBakuDibutuhkanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bahanList[position])
    }

    override fun getItemCount(): Int = bahanList.size
}
