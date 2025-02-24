package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.databinding.ItemStokBumbuBinding
import com.app.gadjahdjaya.model.BahanBaku

class BumbuAdapter(
    private val bumbuList: List<BahanBaku>,
    private val totalStok: Double, // ✅ Total stok semua bumbu
    private val onBumbuClick: () -> Unit // ✅ Klik untuk memunculkan detail popup
) : RecyclerView.Adapter<BumbuAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemStokBumbuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bumbu: BahanBaku) {
            // Hitung persentase
            val persen = if (totalStok > 0) (bumbu.stok / totalStok * 100).toInt() else 0

            // Set data ke UI
            binding.tvNamaBumbu.text = bumbu.nama
            binding.progressStokBumbu.progress = persen
            binding.tvPersentaseStok.text = "$persen%"

            // Klik untuk membuka popup detail
            binding.root.setOnClickListener { onBumbuClick() }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStokBumbuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bumbuList[position])
    }

    override fun getItemCount(): Int = bumbuList.size
}
