package com.app.gadjahdjaya.ui.stokbahan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.gadjahdjaya.databinding.DialogEditBahanBakuBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.FirebaseDatabase

class DialogEditBahanBaku(
    private val bahan: BahanBaku
) : DialogFragment() {

    private var _binding: DialogEditBahanBakuBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditBahanBakuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // **✅ Format stok agar tidak menampilkan desimal jika bilangan bulat**
        val stokFormatted = if (bahan.stok % 1 == 0.0) {
            bahan.stok.toInt().toString() // Jika bilangan bulat, hapus ".0"
        } else {
            bahan.stok.toString() // Jika ada desimal, tetap tampilkan
        }

        // **✅ Set data bahan ke dalam form**
        binding.etNamaBahan.setText(bahan.nama)
        binding.etStokBahan.setText(stokFormatted)

        // **✅ Setup spinner kategori**
        val kategoriList = listOf("bumbu halus", "kecap", "lainnya")
        val kategoriAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kategoriList)
        binding.spinnerKategori.adapter = kategoriAdapter
        binding.spinnerKategori.setSelection(kategoriList.indexOf(bahan.kategori))

        // **✅ Setup spinner satuan**
        val satuanList = listOf("gr", "kg", "butir", "ml", "pcs")
        val satuanAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, satuanList)
        binding.spinnerSatuan.adapter = satuanAdapter
        binding.spinnerSatuan.setSelection(satuanList.indexOf(bahan.satuan))

        // **✅ Tombol Simpan**
        binding.btnSimpanBahan.setOnClickListener { editBahanKeDatabase() }
    }

    private fun editBahanKeDatabase() {
        val nama = binding.etNamaBahan.text.toString().trim()
        val kategori = binding.spinnerKategori.selectedItem?.toString()
        val stokText = binding.etStokBahan.text.toString().trim()
        val satuan = binding.spinnerSatuan.selectedItem?.toString()

        if (nama.isEmpty() || stokText.isEmpty() || kategori.isNullOrEmpty() || satuan.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        val stok = try {
            stokText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Jumlah stok tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        if (stok < 0) {
            Toast.makeText(requireContext(), "Stok tidak boleh negatif!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedBahan = bahan.copy(
            nama = nama,
            kategori = kategori,
            stok = stok,
            satuan = satuan
        )

        database.child(bahan.id).setValue(updatedBahan).addOnSuccessListener {
            Toast.makeText(requireContext(), "Bahan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            dismiss()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Gagal memperbarui bahan. Silakan coba lagi!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        // **✅ Perbesar ukuran dialog**
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
