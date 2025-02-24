package com.app.gadjahdjaya.ui.stokbahan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.gadjahdjaya.databinding.DialogEditBahanPresentaseBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.FirebaseDatabase

class DialogEditBahanPresentase(private val bahan: BahanBaku?) : DialogFragment() {

    private var _binding: DialogEditBahanPresentaseBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditBahanPresentaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // **✅ Format stok agar tidak menampilkan desimal jika bilangan bulat**
        val stokFormatted = bahan?.stok?.let {
            if (it % 1 == 0.0) it.toInt().toString() else it.toString()
        } ?: "0"

        // **✅ Set data ke form**
        bahan?.let { data ->
            binding.etNamaBahan.setText(data.nama)
            binding.etStokBahan.setText(stokFormatted)

            // **✅ Setup spinner kategori**
            val kategoriList = listOf("bumbu halus", "kecap", "lainnya")
            val kategoriAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                kategoriList
            )
            binding.spinnerKategori.adapter = kategoriAdapter
            binding.spinnerKategori.setSelection(kategoriList.indexOf(data.kategori).coerceAtLeast(0))

            // **✅ Setup spinner satuan**
            val satuanList = listOf("gr", "kg", "butir", "ml", "pcs")
            val satuanAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                satuanList
            )
            binding.spinnerSatuan.adapter = satuanAdapter
            binding.spinnerSatuan.setSelection(satuanList.indexOf(data.satuan).coerceAtLeast(0))
        }

        // **✅ Tombol Simpan**
        binding.btnSimpanBahan.setOnClickListener { updateBahan() }
    }

    private fun updateBahan() {
        if (_binding == null) return  // Mencegah crash jika view sudah dihancurkan

        val nama = binding.etNamaBahan.text.toString().trim()
        val kategori = binding.spinnerKategori.selectedItem?.toString() ?: ""
        val stokText = binding.etStokBahan.text.toString().trim()
        val satuan = binding.spinnerSatuan.selectedItem?.toString() ?: ""

        // **✅ Validasi input**
        if (nama.isEmpty() || stokText.isEmpty() || kategori.isEmpty() || satuan.isEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        val stokBaru = try {
            stokText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Jumlah stok tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        if (stokBaru < 0) {
            Toast.makeText(requireContext(), "Stok tidak boleh negatif!", Toast.LENGTH_SHORT).show()
            return
        }

        bahan?.let { data ->
            val stokMaksimum = if (stokBaru > data.stokMaksimum) stokBaru else data.stokMaksimum

            val updatedBahan = data.copy(
                nama = nama,
                kategori = kategori,
                stok = stokBaru,
                stokMaksimum = stokMaksimum,
                satuan = satuan
            )

            val bahanId = data.id.ifEmpty {
                Toast.makeText(requireContext(), "Error: ID bahan tidak ditemukan!", Toast.LENGTH_SHORT).show()
                return
            }

            database.child(bahanId).setValue(updatedBahan)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bahan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal memperbarui bahan!", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "Error: Data bahan tidak valid!", Toast.LENGTH_SHORT).show()
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
