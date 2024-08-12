package com.app.gadjahdjaya

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.DecimalFormat
import java.util.*

class MenuFragment : Fragment(), MenuAdapter.OnItemClickListener {

    private lateinit var recyclerViewMakanan: RecyclerView
    private lateinit var recyclerViewMinuman: RecyclerView
    private lateinit var makananAdapter: MenuAdapter
    private lateinit var minumanAdapter: MenuAdapter
    private lateinit var database: DatabaseReference
    private lateinit var searchEditText: EditText
    private val makananList = mutableListOf<MenuItem>()
    private val minumanList = mutableListOf<MenuItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        recyclerViewMakanan = view.findViewById(R.id.recyclerViewMakanan)
        recyclerViewMinuman = view.findViewById(R.id.recyclerViewMinuman)
        searchEditText = view.findViewById(R.id.searchEditText)

        recyclerViewMakanan.layoutManager = GridLayoutManager(context, 2)
        recyclerViewMinuman.layoutManager = GridLayoutManager(context, 2)

        makananAdapter = MenuAdapter(requireContext(), makananList, this)
        minumanAdapter = MenuAdapter(requireContext(), minumanList, this)
        recyclerViewMakanan.adapter = makananAdapter
        recyclerViewMinuman.adapter = minumanAdapter

        database = FirebaseDatabase.getInstance().getReference("menuItems")

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_tambah_menu)?.setOnClickListener {
            tambahMenu()
        }

        view.findViewById<Button>(R.id.btn_mkn)?.setOnClickListener {
            toggleRecyclerView(true)
        }

        view.findViewById<Button>(R.id.btn_minum)?.setOnClickListener {
            toggleRecyclerView(false)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                makananList.clear()
                minumanList.clear()
                for (dataSnapshot in snapshot.children) {
                    val menuItemMap = dataSnapshot.value as? Map<String, Any>
                    if (menuItemMap != null) {
                        val menuItem = MenuItem(
                            id = dataSnapshot.key ?: "",
                            kategori = menuItemMap["kategori"] as? String ?: "",
                            gambar = menuItemMap["gambar"] as? String ?: "",
                            nama = menuItemMap["nama"] as? String ?: "",
                            harga = (menuItemMap["harga"] as? Long)?.toInt() ?: 0
                        )
                        if (menuItem.kategori.equals("Makanan", ignoreCase = true)) {
                            makananList.add(menuItem)
                        } else if (menuItem.kategori.equals("Minuman", ignoreCase = true)) {
                            minumanList.add(menuItem)
                        }
                    }
                }
                makananAdapter.notifyDataSetChanged()
                minumanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun tambahMenu() {
        val tambahMenuFragment = TambahMenuFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, tambahMenuFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onEditClick(menuItem: MenuItem) {
        val editMenuFragment = EditMenuFragment()
        editMenuFragment.arguments = Bundle().apply {
            putParcelable("menu", menuItem)
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, editMenuFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDeleteClick(menuItem: MenuItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin menghapus menu ini?")
            .setPositiveButton("Ya") { _, _ ->
                hapusMenu(menuItem)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun hapusMenu(menuItem: MenuItem) {
        val menuRef = database.child(menuItem.id)
        menuRef.removeValue()
            .addOnSuccessListener {
                // Jika berhasil dihapus, Anda bisa melakukan tindakan tambahan di sini
            }
            .addOnFailureListener { e ->
                // Jika gagal menghapus, Anda bisa menangani kesalahan di sini
            }
    }

    private fun toggleRecyclerView(showMakanan: Boolean) {
        if (showMakanan) {
            recyclerViewMakanan.visibility = View.VISIBLE
            recyclerViewMinuman.visibility = View.GONE
        } else {
            recyclerViewMakanan.visibility = View.GONE
            recyclerViewMinuman.visibility = View.VISIBLE
        }
    }

    private fun filter(text: String) {
        val filteredMakananList = makananList.filter {
            it.nama.contains(text, ignoreCase = true)
        }
        val filteredMinumanList = minumanList.filter {
            it.nama.contains(text, ignoreCase = true)
        }
        makananAdapter.updateList(filteredMakananList)
        minumanAdapter.updateList(filteredMinumanList)
    }
}
