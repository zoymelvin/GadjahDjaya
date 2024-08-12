package com.app.gadjahdjaya

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class TambahMenuFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null
    private lateinit var spinnerKategori: Spinner

    private lateinit var imageView: ImageView
    private lateinit var etNamaMenu: EditText
    private lateinit var etHargaMenu: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tambah_menu, container, false)

        database = FirebaseDatabase.getInstance().getReference("menuItems")
        storage = FirebaseStorage.getInstance().reference.child("menuImages")

        imageView = view.findViewById(R.id.iv_menu_image)
        etNamaMenu = view.findViewById(R.id.et_nama_menu)
        etHargaMenu = view.findViewById(R.id.et_harga_menu)

        val btnTambahGambar = view.findViewById<Button>(R.id.btn_tambah_gambar)
        btnTambahGambar.setOnClickListener {
            pilihGambarDariGaleri()
        }

        view.findViewById<Button>(R.id.btn_simpan_menu).setOnClickListener {
            simpanMenuKeFirebase()
        }

        spinnerKategori = view.findViewById(R.id.spinner_kategori)

        return view
    }

    private fun pilihGambarDariGaleri() {
        val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galeriIntent, REQUEST_GALERI)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALERI && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri) // Menampilkan gambar yang dipilih di ImageView
        }
    }

    private fun simpanMenuKeFirebase() {
        val namaMenu = etNamaMenu.text.toString()
        val hargaMenu = etHargaMenu.text.toString()
        val kategoriMenu = spinnerKategori.selectedItem.toString()

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
                            val menuItem = HashMap<String, Any>()
                            menuItem["id"] = menuId
                            menuItem["nama"] = namaMenu
                            menuItem["harga"] = hargaMenu.toInt()
                            menuItem["gambar"] = downloadUri.toString()
                            menuItem["kategori"] = kategoriMenu

                            database.child(menuId).setValue(menuItem)
                                .addOnSuccessListener {
                                    if (isAdded) {
                                        Toast.makeText(requireContext(), "Menu berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                    }

                                    // Perbarui data di fragment menu setelah menambahkan menu baru
                                    val menuFragment = MenuFragment()
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, menuFragment)
                                        .commit()
                                }
                                .addOnFailureListener { e ->
                                    if (isAdded) {
                                        Toast.makeText(requireContext(), "Gagal menambahkan menu: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                        } else {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Gagal mendapatkan URL gambar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        if (isAdded) {
                            Toast.makeText(requireContext(), "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Gagal membuat ID menu", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Isi semua data dan pilih gambar!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_GALERI = 100
    }
}
