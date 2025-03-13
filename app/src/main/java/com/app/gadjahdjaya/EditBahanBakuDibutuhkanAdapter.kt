package com.app.gadjahdjaya

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.model.BahanBakuDibutuhkan

class EditBahanBakuDibutuhkanAdapter(
    private val bahanList: MutableList<BahanBakuDibutuhkan>
) : RecyclerView.Adapter<EditBahanBakuDibutuhkanAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaBahan: TextView = view.findViewById(R.id.tv_nama_bahan)
        val etJumlahBahan: EditText = view.findViewById(R.id.et_jumlah_bahan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edit_bahan_baku, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bahan = bahanList[position]
        holder.tvNamaBahan.text = bahan.namaBahan
        holder.etJumlahBahan.setText(bahan.jumlah.toString())

        holder.etJumlahBahan.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val jumlah = s.toString().toDoubleOrNull() ?: 0.0
                bahan.jumlah = jumlah
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount(): Int = bahanList.size
}
