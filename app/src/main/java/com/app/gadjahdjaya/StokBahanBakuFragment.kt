package com.app.gadjahdjaya.ui.stokbahan

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.adapter.BahanBakuAdapter
import com.app.gadjahdjaya.adapter.KategoriBumbuAdapter
import com.app.gadjahdjaya.databinding.FragmentStokBahanBakuBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.*

class StokBahanBakuFragment : Fragment() {

    private var _binding: FragmentStokBahanBakuBinding? = null
    private val binding get() = _binding!!

    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")
    private val kategoriMap = mutableMapOf<String, MutableList<BahanBaku>>()
    private val bahanList = mutableListOf<BahanBaku>()
    private val bahanPersentaseList = mutableListOf<BahanBaku>()
    private val searchHandler = Handler(Looper.getMainLooper())

    private lateinit var bahanAdapter: BahanBakuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStokBahanBakuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewBumbuPersentase.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewBahanBaku.layoutManager = LinearLayoutManager(requireContext())

        bahanAdapter = BahanBakuAdapter(bahanList) { bahan -> showDetailBahanDialog(bahan) }
        binding.recyclerViewBahanBaku.adapter = bahanAdapter

        binding.btnTambahStok.setOnClickListener { showTambahBahanDialog() }
        binding.cardTotalStok.setOnClickListener { replaceFragment(FragmentTotalStock()) }
        binding.cardStokMenipis.setOnClickListener { replaceFragment(StokMenipisFragment()) }

        loadBahanBaku()
        loadTotalStock()
        loadStokMenipis()
    }

    /**
     * 🚀 **Load data bahan baku dengan FIX konversi Long ke Double**
     */
    private fun loadBahanBaku() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                kategoriMap.clear()
                bahanList.clear()
                bahanPersentaseList.clear()

                for (data in snapshot.children) {
                    try {
                        val bahanMap = data.value as? Map<String, Any>
                        if (bahanMap != null) {
                            val bahan = parseBahanBaku(data.key ?: "", bahanMap)
                            if (bahan.kategori.equals("bumbu halus", true) ||
                                bahan.kategori.equals("kecap", true) ||
                                bahan.kategori.equals("protein", true) ||
                                bahan.kategori.equals("mie", true) ||
                                bahan.kategori.equals("sayur", true) ||
                                bahan.kategori.equals("minyak goreng", true)) {
                                kategoriMap.getOrPut(bahan.kategori) { mutableListOf() }.add(bahan)
                                bahanPersentaseList.add(bahan)
                            } else {
                                bahanList.add(bahan)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "❌ Gagal parsing bahan ${data.key}: ${e.message}")
                    }
                }

                binding.recyclerViewBumbuPersentase.adapter = KategoriBumbuAdapter(
                    kategoriMap = kategoriMap,
                    onKategoriClick = { kategori, bumbuList -> showDetailBumbuDialog(kategori, bumbuList) }
                )

                bahanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "❌ Gagal memuat data bahan: ${error.message}")
            }
        })
    }

    /**
     * 🚀 **Load Total Stok**
     */
    private fun loadTotalStock() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                val totalStock = snapshot.childrenCount.toInt()
                binding.valueTotalStok.text = totalStock.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "❌ Gagal memuat total stok: ${error.message}")
            }
        })
    }

    /**
     * 🚀 **Load Stok Menipis**
     */
    private fun loadStokMenipis() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return

                var stokMenipisCount = 0

                for (data in snapshot.children) {
                    try {
                        val bahanMap = data.value as? Map<String, Any>
                        if (bahanMap != null) {
                            val bahan = parseBahanBaku(data.key ?: "", bahanMap)

                            // ✅ Hanya tambahkan ke stok menipis jika stok <= 20% dari stok maksimum
                            if (bahan.stok <= (bahan.stokMaksimum * 0.2)) {
                                stokMenipisCount++
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseError", "❌ Gagal parsing bahan ${data.key}: ${e.message}")
                    }
                }

                // ✅ Pastikan angka di CardView sesuai dengan jumlah stok menipis yang ditemukan
                binding.valueStokMenipis.text = if (stokMenipisCount > 0) stokMenipisCount.toString() else "0"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "❌ Gagal memuat stok menipis: ${error.message}")
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

    private fun showTambahBahanDialog() {
        val dialog = DialogTambahBahan()
        dialog.show(childFragmentManager, "DialogTambahBahan")
    }

    private fun showDetailBahanDialog(bahan: BahanBaku) {
        val dialog = DialogDetailBahan(bahan)
        dialog.show(childFragmentManager, "DialogDetailBahan")
    }

    private fun showDetailBumbuDialog(kategori: String, bumbuList: List<BahanBaku>) {
        val dialog = DialogDetailBumbu(kategori, bumbuList)
        dialog.show(childFragmentManager, "DialogDetailBumbu")
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
