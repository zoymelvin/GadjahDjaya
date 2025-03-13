package com.app.gadjahdjaya

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.model.BahanBakuDibutuhkan
import com.app.gadjahdjaya.model.MenuItem
import com.app.gadjahdjaya.ui.menu.MenuFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class EditMenuFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private var imageUri: Uri? = null

    private lateinit var spinnerKategori: Spinner
    private lateinit var imageView: ImageView
    private lateinit var etNamaMenu: EditText
    private lateinit var etHargaMenu: EditText
    private lateinit var bahanAdapter: EditBahanBakuDibutuhkanAdapter
    private lateinit var rvDaftarBahanBaku: RecyclerView

    private var menuItem: MenuItem? = null
    private var bahanList = mutableListOf<BahanBakuDibutuhkan>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_menu, container, false)

        database = FirebaseDatabase.getInstance().getReference("menuItems")
        storage = FirebaseStorage.getInstance().reference.child("menuImages")

        imageView = view.findViewById(R.id.menu_image)
        etNamaMenu = view.findViewById(R.id.et_nama_menu)
        etHargaMenu = view.findViewById(R.id.et_harga_menu)
        spinnerKategori = view.findViewById(R.id.spinner_kategori_menu)

        rvDaftarBahanBaku = view.findViewById(R.id.rv_daftar_bahan_baku)
        bahanAdapter = EditBahanBakuDibutuhkanAdapter(bahanList)

        rvDaftarBahanBaku.layoutManager = LinearLayoutManager(requireContext())
        rvDaftarBahanBaku.adapter = bahanAdapter

        view.findViewById<Button>(R.id.iv_menu_image).setOnClickListener {
            pilihGambarDariGaleri()
        }

        view.findViewById<Button>(R.id.btn_simpan_menu).setOnClickListener {
            simpanMenuKeFirebase()
        }

        etHargaMenu.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                etHargaMenu.removeTextChangedListener(this)
                try {
                    val originalString = s.toString().replace(".", "")
                    val longval = originalString.toLong()
                    val formatter: NumberFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale("in", "ID")))
                    val formattedString = formatter.format(longval)
                    etHargaMenu.setText(formattedString)
                    etHargaMenu.setSelection(etHargaMenu.text.length)
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }
                etHargaMenu.addTextChangedListener(this)
            }
        })

        menuItem = arguments?.getParcelable("menu")
        menuItem?.let { item ->
            etNamaMenu.setText(item.nama)
            etHargaMenu.setText(item.harga.toString())
            spinnerKategori.setSelection(getSpinnerIndex(spinnerKategori, item.kategori))
            Picasso.get().load(item.gambar).into(imageView)
            bahanList.clear()
            bahanList.addAll(item.bahanBakuDibutuhkan ?: emptyList())
            bahanAdapter.notifyDataSetChanged()
        }

        return view
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) return i
        }
        return 0
    }

    private fun pilihGambarDariGaleri() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALERI)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALERI && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun simpanMenuKeFirebase() {
        val namaMenu = etNamaMenu.text.toString()
        val hargaMenu = etHargaMenu.text.toString().replace(".", "").toIntOrNull()
        val kategoriMenu = spinnerKategori.selectedItem.toString()

        if (namaMenu.isEmpty() || hargaMenu == null) {
            Toast.makeText(requireContext(), "Isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        menuItem?.let { item ->
            val menuRef = database.child(item.id)

            if (imageUri != null) {
                val imageRef = storage.child("${item.id}.jpg")
                imageRef.putFile(imageUri!!).continueWithTask { task ->
                    if (!task.isSuccessful) task.exception?.let { throw it }
                    imageRef.downloadUrl
                }.addOnSuccessListener { uri ->
                    item.gambar = uri.toString()
                    updateMenu(menuRef, item, namaMenu, hargaMenu, kategoriMenu)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal upload gambar", Toast.LENGTH_SHORT).show()
                }
            } else {
                updateMenu(menuRef, item, namaMenu, hargaMenu, kategoriMenu)
            }
        }
    }

    private fun updateMenu(menuRef: DatabaseReference, item: MenuItem, nama: String, harga: Int, kategori: String) {
        item.nama = nama
        item.harga = harga
        item.kategori = kategori
        item.bahanBakuDibutuhkan = bahanList

        menuRef.setValue(item).addOnSuccessListener {
            Toast.makeText(requireContext(), "Menu diperbarui", Toast.LENGTH_SHORT).show()
            parentFragmentManager.beginTransaction().replace(R.id.fragment_container, MenuFragment()).commit()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_GALERI = 100
    }
}
