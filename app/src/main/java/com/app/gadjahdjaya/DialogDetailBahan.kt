package com.app.gadjahdjaya.ui.stokbahan

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.gadjahdjaya.databinding.DialogDetailBahanBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.FirebaseDatabase

class DialogDetailBahan(private val bahan: BahanBaku) : DialogFragment() {

    private var _binding: DialogDetailBahanBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogDetailBahanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // **✅ Perbaikan Format Stok agar tidak dalam format desimal jika bilangan bulat**
        val stokFormatted = if (bahan.stok % 1 == 0.0) {
            bahan.stok.toInt().toString() // Jika bilangan bulat, hapus ".0"
        } else {
            bahan.stok.toString() // Jika ada desimal, tetap tampilkan
        }

        // Set data bahan baku
        binding.tvNamaBahanDetail.text = "Nama Bahan: ${bahan.nama}"
        binding.tvTanggalMasukDetail.text = "Tanggal Masuk: ${bahan.tanggalMasuk}"
        binding.tvStokBahanDetail.text = "Stok: $stokFormatted ${bahan.satuan}"

        // **✅ Tombol Edit**
        binding.icEdit.setOnClickListener {
            val dialog = DialogEditBahanBaku(bahan)
            dialog.show(parentFragmentManager, "DialogEditBahan")
            dismiss()
        }

        // **✅ Tombol Hapus dengan Konfirmasi**
        binding.icDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        // **✅ Tombol Tambah Stok**
        binding.icPluss.setOnClickListener {
            val dialog = DialogTambahStok(bahan)
            dialog.show(parentFragmentManager, "DialogTambahStok")
        }
    }

    // **✅ Fungsi Menampilkan Dialog Konfirmasi Sebelum Menghapus Bahan**
    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus bahan '${bahan.nama}'?")
            .setPositiveButton("Ya") { _, _ -> deleteBahan() } // Jika konfirmasi, panggil deleteBahan()
            .setNegativeButton("Tidak", null) // Jika dibatalkan, tidak melakukan apa-apa
            .show()
    }

    // **✅ Fungsi Menghapus Bahan dari Firebase**
    private fun deleteBahan() {
        database.child(bahan.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Bahan berhasil dihapus!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menghapus bahan!", Toast.LENGTH_SHORT).show()
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
