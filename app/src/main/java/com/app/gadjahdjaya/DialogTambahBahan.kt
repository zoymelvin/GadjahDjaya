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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class DialogTambahBahan(private val bahan: BahanBaku? = null) : DialogFragment() {

    private var _binding: DialogTambahBahanBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogTambahBahanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Setup spinner kategori
        val kategoriList = listOf("bumbu halus", "kecap", "protein", "mie", "sayur", "minyak goreng", "lainnya")
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
     * ✅ Fungsi untuk menyimpan bahan ke Firebase & mencatat log pemasukan
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
        val stokTambahan = stokInput.toDoubleOrNull() ?: 0.0
        if (stokTambahan <= 0) {
            Toast.makeText(requireContext(), "Stok harus lebih dari 0!", Toast.LENGTH_SHORT).show()
            return
        }

        val tanggalMasuk = bahan?.tanggalMasuk ?: System.currentTimeMillis().toString()

        val bahanRef = database.child("bahanBaku").child(bahan?.id ?: database.child("bahanBaku").push().key!!)
        val logRef = database.child("log_stok").child(getCurrentDate()).child("pemasukan").push()

        // 🔹 **Update stok di `bahan_baku`**
        bahanRef.get().addOnSuccessListener { snapshot ->
            val stokSaatIni = snapshot.child("stok").getValue(Double::class.java) ?: 0.0
            val stokBaru = stokSaatIni + stokTambahan

            val updatedBahan = BahanBaku(
                id = bahan?.id ?: bahanRef.key!!,
                nama = nama,
                kategori = kategori,
                stok = stokBaru,
                stokMaksimum = bahan?.stokMaksimum ?: stokBaru,
                satuan = satuan,
                tanggalMasuk = tanggalMasuk
            )

            bahanRef.setValue(updatedBahan).addOnSuccessListener {
                // 🔹 **Tambahkan log pemasukan ke `log_stok`**
                val logData = mapOf(
                    "nama" to nama,
                    "jumlah" to stokTambahan,
                    "satuan" to satuan,
                    "waktu" to getCurrentTime(),
                )

                logRef.setValue(logData).addOnSuccessListener {
                    Toast.makeText(requireContext(), "Bahan berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal mencatat log pemasukan!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menyimpan bahan!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Gagal mengambil data bahan!", Toast.LENGTH_SHORT).show()
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

    /**
     * ✅ Fungsi mendapatkan tanggal saat ini dalam format `YYYY-MM-DD`
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * ✅ Fungsi mendapatkan waktu saat ini dalam format `HH:mm:ss`
     */
    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(Date())
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
