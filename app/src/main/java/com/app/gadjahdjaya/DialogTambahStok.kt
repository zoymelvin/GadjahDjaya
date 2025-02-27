package com.app.gadjahdjaya.ui.stokbahan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.gadjahdjaya.databinding.DialogTambahStokBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class DialogTambahStok(private val bahan: BahanBaku) : DialogFragment() {

    private var _binding: DialogTambahStokBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogTambahStokBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Pastikan stok tampil dalam format yang benar (tanpa ".0" jika integer)
        val stokFormatted = if (bahan.stok % 1 == 0.0) bahan.stok.toInt().toString() else bahan.stok.toString()

        // ✅ Tampilkan informasi stok saat ini
        binding.txtNamaBahan.text = bahan.nama
        binding.txtStokSaatIni.text = "Stok saat ini: $stokFormatted ${bahan.satuan}"

        // 🔹 Tombol Tambah Stok
        binding.btnTambah.setOnClickListener {
            val jumlahTambahStr = binding.inputJumlahTambah.text.toString().trim()

            if (jumlahTambahStr.isEmpty()) {
                Toast.makeText(requireContext(), "Masukkan jumlah yang valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val jumlahTambah = jumlahTambahStr.toDoubleOrNull() ?: 0.0
            if (jumlahTambah > 0) {
                tambahStok(jumlahTambah)
            } else {
                Toast.makeText(requireContext(), "Jumlah harus lebih dari 0!", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔹 Tombol Batal
        binding.btnBatal.setOnClickListener {
            dismiss()
        }
    }

    /**
     * ✅ **Fungsi untuk menambah stok & mencatat log pemasukan stok**
     */
    private fun tambahStok(jumlahTambah: Double) {
        val stokBaru = bahan.stok + jumlahTambah
        val stokMaksimumBaru = if (stokBaru > bahan.stokMaksimum) stokBaru else bahan.stokMaksimum


        // ✅ Referensi ke `bahan_baku` dan `log_stok`
        val bahanRef = database.child("bahanBaku").child(bahan.id)
        val logRef = database.child("log_stok").child(getCurrentDate()).child("pemasukan").push()

        // 🔹 **Update stok bahan baku**
        val updateData = mapOf(
            "stok" to stokBaru,
            "stokMaksimum" to stokMaksimumBaru,
            "tanggalMasuk" to getCurrentTimestamp()
        )

        bahanRef.updateChildren(updateData)
            .addOnSuccessListener {
                // 🔹 **Tambahkan Log Pemasukan**
                val logData = mapOf(
                    "nama" to bahan.nama,
                    "jumlah" to jumlahTambah,
                    "satuan" to bahan.satuan,
                    "waktu" to getCurrentTimestamp(),
                )

                logRef.setValue(logData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Stok berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal mencatat log pemasukan!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memperbarui stok!", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * ✅ **Fungsi mendapatkan tanggal hari ini dalam format `YYYY-MM-DD`**
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * ✅ **Fungsi mendapatkan timestamp real-time dalam format `dd MMM yyyy HH:mm`**
     */
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
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
