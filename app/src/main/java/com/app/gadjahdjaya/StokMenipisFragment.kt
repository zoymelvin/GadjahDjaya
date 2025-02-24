package com.app.gadjahdjaya.ui.stokbahan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gadjahdjaya.adapter.BahanBakuAdapter
import com.app.gadjahdjaya.databinding.FragmentStokMenipisBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.*

class StokMenipisFragment : Fragment() {

    private var _binding: FragmentStokMenipisBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")
    private val stokMenipisList = mutableListOf<BahanBaku>()
    private lateinit var bahanAdapter: BahanBakuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStokMenipisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Setup RecyclerView
        binding.recyclerViewStokMenipis.layoutManager = LinearLayoutManager(requireContext())
        bahanAdapter = BahanBakuAdapter(stokMenipisList) { bahan -> showDetailBahanDialog(bahan) }
        binding.recyclerViewStokMenipis.adapter = bahanAdapter

        // ✅ Load data stok menipis
        loadStokMenipis()
    }

    /**
     * 🚀 **Load data stok menipis dari Firebase**
     */
    private fun loadStokMenipis() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                stokMenipisList.clear()

                for (data in snapshot.children) {
                    try {
                        val bahanMap = data.value as? Map<String, Any>
                        if (bahanMap != null) {
                            val bahan = parseBahanBaku(data.key ?: "", bahanMap)
                            if (bahan.stok <= (bahan.stokMaksimum * 0.2)) {
                                stokMenipisList.add(bahan)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "❌ Gagal parsing bahan ${data.key}: ${e.message}")
                    }
                }

                // ✅ Pastikan adapter diperbarui setelah data di-load
                if (stokMenipisList.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada stok menipis!", Toast.LENGTH_SHORT).show()
                }
                bahanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "❌ Gagal memuat data stok menipis: ${error.message}")
            }
        })
    }

    /**
     * 🛠 **Konversi aman Long ke Double untuk Firebase**
     */
    private fun parseBahanBaku(id: String, data: Map<String, Any>): BahanBaku {
        return BahanBaku(
            id = id,
            nama = data["nama"] as? String ?: "",
            kategori = data["kategori"] as? String ?: "",
            stok = (data["stok"] as? Number)?.toDouble() ?: 0.0,
            stokMaksimum = (data["stokMaksimum"] as? Number)?.toDouble() ?: 0.0,
            satuan = data["satuan"] as? String ?: "",
            tanggalMasuk = data["tanggalMasuk"] as? String ?: ""
        )
    }

    private fun showDetailBahanDialog(bahan: BahanBaku) {
        val dialog = DialogDetailBahan(bahan)
        dialog.show(childFragmentManager, "DialogDetailBahan")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
