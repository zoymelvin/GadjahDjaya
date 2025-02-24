package com.app.gadjahdjaya.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gadjahdjaya.adapter.BahanBakuPilihAdapter
import com.app.gadjahdjaya.databinding.DialogPilihBahanBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.*

class DialogPilihBahan(
    private val onBahanDipilih: (BahanBaku, Double) -> Unit
) : DialogFragment() {

    private var _binding: DialogPilihBahanBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")
    private val bahanList = mutableListOf<BahanBaku>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogPilihBahanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Setup RecyclerView
        binding.recyclerBahanBaku.layoutManager = LinearLayoutManager(requireContext())

        // ✅ Load data dari Firebase
        loadBahanDariFirebase()

        // ✅ Tombol "Tutup" untuk menutup dialog
        binding.btnTutup.setOnClickListener { dismiss() }
    }

    /**
     * 🚀 **Fungsi untuk mengambil data bahan baku dari Firebase**
     */
    private fun loadBahanDariFirebase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return  // Hindari akses saat fragment dihancurkan
                bahanList.clear()

                for (data in snapshot.children) {
                    try {
                        val bahanMap = data.value as? Map<String, Any>
                        if (bahanMap != null) {
                            val bahan = parseBahanBaku(data.key ?: "", bahanMap)
                            bahanList.add(bahan)
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "❌ Gagal parsing bahan ${data.key}: ${e.message}")
                    }
                }

                if (bahanList.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada bahan baku tersedia", Toast.LENGTH_SHORT).show()
                }

                binding.recyclerBahanBaku.adapter = BahanBakuPilihAdapter(bahanList, onBahanDipilih)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "❌ Gagal mengambil data bahan: ${error.message}")
                Toast.makeText(requireContext(), "Gagal memuat data bahan baku!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * 🛠 **Fungsi untuk mengonversi data dari Firebase ke `BahanBaku` tanpa error Long ke Double**
     */
    private fun parseBahanBaku(id: String, data: Map<String, Any>): BahanBaku {
        return BahanBaku(
            id = id,
            nama = data["nama"] as? String ?: "",
            kategori = data["kategori"] as? String ?: "",
            stok = when (val stokValue = data["stok"]) {
                is Long -> stokValue.toDouble()  // 🔥 Konversi aman dari Long ke Double
                is Double -> stokValue
                else -> 0.0
            },
            stokMaksimum = when (val stokMax = data["stokMaksimum"]) {
                is Long -> stokMax.toDouble()
                is Double -> stokMax
                else -> 0.0
            },
            satuan = data["satuan"] as? String ?: "",
            tanggalMasuk = data["tanggalMasuk"] as? String ?: ""
        )
    }

    override fun onStart() {
        super.onStart()
        // ✅ Perbesar ukuran dialog agar mudah digunakan
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
