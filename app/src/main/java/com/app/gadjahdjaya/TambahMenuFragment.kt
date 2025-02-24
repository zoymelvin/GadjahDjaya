package com.app.gadjahdjaya.ui.menu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gadjahdjaya.adapter.BahanBakuDibutuhkanAdapter
import com.app.gadjahdjaya.databinding.FragmentTambahMenuBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.app.gadjahdjaya.model.BahanBakuDibutuhkan
import com.app.gadjahdjaya.model.MenuItem
import com.google.firebase.database.*

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class TambahMenuFragment : Fragment() {

    private var _binding: FragmentTambahMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var bahanDatabase: DatabaseReference
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null
    private lateinit var spinnerKategori: Spinner
    private lateinit var bahanAdapter: BahanBakuDibutuhkanAdapter
    private val bahanDibutuhkanList = mutableListOf<BahanBakuDibutuhkan>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("menuItems")
        bahanDatabase = FirebaseDatabase.getInstance().getReference("bahanBaku")
        storage = FirebaseStorage.getInstance().reference.child("menuImages")

        spinnerKategori = binding.spinnerKategori

        // ✅ Setup RecyclerView untuk bahan baku
        binding.recyclerBahanBaku.layoutManager = LinearLayoutManager(requireContext())
        bahanAdapter = BahanBakuDibutuhkanAdapter(bahanDibutuhkanList) { bahan ->
            bahanDibutuhkanList.remove(bahan)
            bahanAdapter.notifyDataSetChanged()
        }
        binding.recyclerBahanBaku.adapter = bahanAdapter

        // ✅ Pilih gambar dari galeri
        binding.btnTambahGambar.setOnClickListener { pilihGambarDariGaleri() }

        // ✅ Tambahkan bahan baku
        binding.btnTambahBahan.setOnClickListener {
            val dialog = DialogPilihBahan { bahan, jumlah ->
                // ✅ Pastikan kita menyimpan idBahan dari Firebase
                val bahanRef = bahanDatabase.orderByChild("nama").equalTo(bahan.nama)
                bahanRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val idBahan = data.key ?: ""
                                val bahanDipilih = BahanBakuDibutuhkan(
                                    idBahan = idBahan,
                                    namaBahan = bahan.nama,
                                    jumlah = jumlah,
                                    satuan = bahan.satuan
                                )
                                bahanDibutuhkanList.add(bahanDipilih)
                                bahanAdapter.notifyDataSetChanged()
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Gagal mendapatkan ID bahan", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            dialog.show(parentFragmentManager, "DialogPilihBahan")
        }

        // ✅ Simpan menu ke Firebase
        binding.btnSimpanMenu.setOnClickListener { simpanMenuKeFirebase() }
    }

    private fun pilihGambarDariGaleri() {
        val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galeriIntent, REQUEST_GALERI)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALERI && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.ivMenuImage.setImageURI(imageUri)
        }
    }

    private fun simpanMenuKeFirebase() {
        val namaMenu = binding.etNamaMenu.text.toString()
        val hargaMenu = binding.etHargaMenu.text.toString()
        val kategoriMenu = spinnerKategori.selectedItem.toString()
        val deskripsiMenu = binding.etDeskripsiMenu.text.toString()

        if (namaMenu.isNotEmpty() && hargaMenu.isNotEmpty() && imageUri != null) {
            val menuId = database.push().key
            if (menuId != null) {
                val imageRef = storage.child("$menuId.jpg")
                imageRef.putFile(imageUri!!)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        imageRef.downloadUrl
                    }
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            val menuItem = MenuItem(
                                id = menuId,
                                nama = namaMenu,
                                harga = hargaMenu.toInt(),
                                gambar = downloadUri.toString(),
                                kategori = kategoriMenu,
                                deskripsi = deskripsiMenu,
                                bahanBakuDibutuhkan = bahanDibutuhkanList
                            )

                            database.child(menuId).setValue(menuItem)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                    parentFragmentManager.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Gagal menyimpan menu!", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
        } else {
            Toast.makeText(requireContext(), "Isi semua data dan pilih gambar!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_GALERI = 100
    }
}
