package com.app.gadjahdjaya.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.databinding.ItemBumbuDetailBinding
import com.app.gadjahdjaya.model.BahanBaku

class BumbuDetailAdapter(
    private val bumbuList: List<BahanBaku>,
    private val onEditClick: (BahanBaku) -> Unit,
    private val onDeleteClick: (BahanBaku) -> Unit,
    private val onTambahClick: (BahanBaku) -> Unit
) : RecyclerView.Adapter<BumbuDetailAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemBumbuDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bumbu: BahanBaku) {
            val persen = if (bumbu.stokMaksimum > 0) {
                ((bumbu.stok / bumbu.stokMaksimum) * 100).toInt()
            } else {
                0
            }

            binding.tvBumbuNama.text = bumbu.nama
            binding.tvBumbuStok.text = "$persen%"
            binding.progressBumbu.progress = persen

            binding.icEdit.setOnClickListener { onEditClick(bumbu) }
            binding.icDelete.setOnClickListener { onDeleteClick(bumbu) }
            binding.icPluss.setOnClickListener { onTambahClick(bumbu) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBumbuDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(bumbuList[position])
    }

    override fun getItemCount(): Int = bumbuList.size
}
