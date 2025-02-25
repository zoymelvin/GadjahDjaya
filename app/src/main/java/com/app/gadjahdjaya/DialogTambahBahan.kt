package com.app.gadjahdjaya.ui.stokbahan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.gadjahdjaya.databinding.DialogTambahBahanBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.FirebaseDatabase

class DialogTambahBahan(private val bahan: BahanBaku? = null) : DialogFragment() {

    private var _binding: DialogTambahBahanBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogTambahBahanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Setup spinner kategori
        val kategoriList = listOf("bumbu halus", "kecap", "protein" ,"mie", "sayur", "minyak goreng", "lainnya")
        val kategoriAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, kategoriList)
        binding.spinnerKategori.adapter = kategoriAdapter

        // ✅ Setup spinner satuan
        val satuanList = listOf("gr", "kg", "butir", "ml", "pcs")
        val satuanAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, satuanList)
        binding.spinnerSatuan.adapter = satuanAdapter

        // ✅ Jika mode edit, isi data yang ada ke dalam form
        bahan?.let {
            binding.etNamaBahan.setText(it.nama)
            binding.spinnerKategori.setSelection(kategoriList.indexOf(it.kategori))
            binding.etStokBahan.setText(formatStok(it.stok))  // Format stok agar tidak 15.0
            binding.spinnerSatuan.setSelection(satuanList.indexOf(it.satuan))
        }

        // ✅ Tombol Simpan
        binding.btnSimpanBahan.setOnClickListener { simpanBahanKeDatabase() }
    }

    /**
     * ✅ Fungsi untuk menyimpan bahan ke Firebase
     */
    private fun simpanBahanKeDatabase() {
        val nama = binding.etNamaBahan.text.toString().trim()
        val kategori = binding.spinnerKategori.selectedItem?.toString()
        val stokInput = binding.etStokBahan.text.toString().trim()
        val satuan = binding.spinnerSatuan.selectedItem?.toString()

        if (nama.isEmpty() || stokInput.isEmpty() || kategori.isNullOrEmpty() || satuan.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Harap isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ Konversi stok agar tetap mendukung angka desimal
        val stokAwal = stokInput.toDoubleOrNull() ?: 0.0
        val tanggalMasuk = bahan?.tanggalMasuk ?: System.currentTimeMillis().toString()

        val updatedBahan = BahanBaku(
            id = bahan?.id ?: database.push().key!!,  // ✅ Pastikan ID tidak kosong
            nama = nama,
            kategori = kategori,
            stok = stokAwal,
            stokMaksimum = bahan?.stokMaksimum ?: stokAwal, // Jika edit, gunakan stokMaksimum sebelumnya
            satuan = satuan,
            tanggalMasuk = tanggalMasuk
        )

        database.child(updatedBahan.id).setValue(updatedBahan).addOnSuccessListener {
            Toast.makeText(requireContext(), "Bahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
            dismiss()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Gagal menyimpan bahan!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * ✅ Fungsi untuk memastikan tampilan stok tidak menampilkan ".0" jika angka bulat
     */
    private fun formatStok(stok: Double): String {
        return if (stok % 1.0 == 0.0) {
            stok.toInt().toString()  // Jika stok bulat, tampilkan sebagai integer (15 → "15")
        } else {
            stok.toString()  // Jika ada desimal, biarkan seperti itu (1.5 → "1.5")
        }
    }

    override fun onStart() {
        super.onStart()
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
