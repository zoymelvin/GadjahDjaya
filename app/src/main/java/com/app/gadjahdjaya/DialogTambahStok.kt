package com.app.gadjahdjaya.ui.stokbahan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.gadjahdjaya.databinding.DialogTambahStokBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class DialogTambahStok(private val bahan: BahanBaku) : DialogFragment() {

    private var _binding: DialogTambahStokBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogTambahStokBinding.inflate(inflater, container, false)
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

        // **✅ Tampilkan nama bahan & stok saat ini**
        binding.txtNamaBahan.text = bahan.nama
        binding.txtStokSaatIni.text = "Stok saat ini: $stokFormatted ${bahan.satuan}"

        // **🔹 Tombol Tambah Stok**
        binding.btnTambah.setOnClickListener {
            val jumlahTambahStr = binding.inputJumlahTambah.text.toString().trim()

            if (jumlahTambahStr.isEmpty()) {
                Toast.makeText(requireContext(), "Masukkan jumlah yang valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jumlahTambah = jumlahTambahStr.toIntOrNull() ?: 0
            if (jumlahTambah > 0) {
                tambahStok(jumlahTambah)
            } else {
                Toast.makeText(requireContext(), "Jumlah harus lebih dari 0!", Toast.LENGTH_SHORT).show()
            }
        }

        // **🔹 Tombol Batal**
        binding.btnBatal.setOnClickListener {
            dismiss()
        }
    }

    private fun tambahStok(jumlahTambah: Int) {
        val stokBaru = bahan.stok + jumlahTambah
        val stokMaksimumBaru = if (stokBaru > bahan.stokMaksimum) stokBaru else bahan.stokMaksimum

        // **✅ Dapatkan tanggal & jam real-time**
        val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        val tanggalBaru = sdf.format(Date())

        // **✅ Perbaikan Format Stok**
        val stokBaruFormatted = if (stokBaru % 1 == 0.0) stokBaru.toInt() else stokBaru
        val stokMaksimumFormatted = if (stokMaksimumBaru % 1 == 0.0) stokMaksimumBaru.toInt() else stokMaksimumBaru

        // **✅ Update ke Firebase**
        val updateData = mapOf(
            "stok" to stokBaruFormatted,
            "stokMaksimum" to stokMaksimumFormatted,
            "tanggalMasuk" to tanggalBaru // **🕒 Update waktu**
        )

        database.child(bahan.id).updateChildren(updateData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Stok berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memperbarui stok!", Toast.LENGTH_SHORT).show()
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
