package com.app.gadjahdjaya

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.gadjahdjaya.model.MenuItem
import com.app.gadjahdjaya.ui.menu.MenuFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class EditMenuFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null
    private lateinit var spinnerKategori: Spinner
    private lateinit var imageView: ImageView
    private lateinit var etNamaMenu: EditText
    private lateinit var etHargaMenu: EditText

    private var menuItem: MenuItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_menu, container, false)

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

        // Mengambil data MenuItem dari argumen
        menuItem = arguments?.getParcelable("menu")
        menuItem?.let { item ->
            etNamaMenu.setText(item.nama)
            etHargaMenu.setText(item.harga.toString())
            spinnerKategori.setSelection(getSpinnerIndex(spinnerKategori, item.kategori))
            Picasso.get().load(item.gambar).into(imageView)
        }

        return view
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
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
        val hargaMenuText = etHargaMenu.text.toString()
        val hargaMenu = hargaMenuText.toIntOrNull()
        val kategoriMenu = spinnerKategori.selectedItem.toString()

        if (namaMenu.isEmpty() || hargaMenu == null || hargaMenu <= 0) {
            Toast.makeText(requireContext(), "Isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        menuItem?.let { item ->
            val menuRef = database.child(item.id)

            if (imageUri != null) {
                val imageRef = storage.child("${item.id}.jpg")
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
                            item.gambar = downloadUri.toString()
                            updateMenuInDatabase(menuRef, item)
                        } else {
                            Toast.makeText(requireContext(), "Gagal mendapatkan URL gambar", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Gagal mengunggah gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                updateMenuInDatabase(menuRef, item)
            }
        } ?: run {
            Toast.makeText(requireContext(), "Menu item tidak ditemukan!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMenuInDatabase(menuRef: DatabaseReference, menuItem: MenuItem) {
        menuItem.nama = etNamaMenu.text.toString()
        menuItem.harga = etHargaMenu.text.toString().toInt()
        menuItem.kategori = spinnerKategori.selectedItem.toString()

        menuRef.setValue(menuItem)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Menu berhasil diperbarui", Toast.LENGTH_SHORT).show()
                // Mengarahkan pengguna kembali ke MenuFragment setelah berhasil memperbarui menu
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MenuFragment())
                    .commit()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal memperbarui menu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        private const val REQUEST_GALERI = 100
    }
}
