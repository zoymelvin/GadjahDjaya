package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.databinding.ItemPilihBahanBinding
import com.app.gadjahdjaya.model.BahanBaku

class BahanBakuPilihAdapter(
    private val bahanList: List<BahanBaku>,
    private val onPilihClick: (BahanBaku, Double) -> Unit
) : RecyclerView.Adapter<BahanBakuPilihAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemPilihBahanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bahan: BahanBaku) {
            binding.tvNamaBahan.text = bahan.nama

            binding.btnPilih.setOnClickListener {
                val jumlah = binding.etJumlahBahan.text.toString().toDoubleOrNull() ?: 0.0
                if (jumlah > 0) {
                    onPilihClick(bahan, jumlah)
                } else {
                    binding.etJumlahBahan.error = "Masukkan jumlah yang valid"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPilihBahanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bahanList[position])
    }

    override fun getItemCount(): Int = bahanList.size
}
