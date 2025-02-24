package com.app.gadjahdjaya.ui.menu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.app.gadjahdjaya.EditMenuFragment
import com.app.gadjahdjaya.MenuAdapter
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.databinding.FragmentMenuBinding
import com.app.gadjahdjaya.model.MenuItem
import com.google.firebase.database.*

class MenuFragment : Fragment(), MenuAdapter.OnItemClickListener {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var makananAdapter: MenuAdapter
    private lateinit var minumanAdapter: MenuAdapter
    private val makananList = mutableListOf<MenuItem>()
    private val minumanList = mutableListOf<MenuItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("menuItems")

        // ✅ Setup RecyclerView
        binding.recyclerViewMakanan.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerViewMinuman.layoutManager = GridLayoutManager(requireContext(), 2)

        makananAdapter = MenuAdapter(requireContext(), makananList, this)
        minumanAdapter = MenuAdapter(requireContext(), minumanList, this)

        binding.recyclerViewMakanan.adapter = makananAdapter
        binding.recyclerViewMinuman.adapter = minumanAdapter

        // ✅ Setup tombol filter makanan & minuman
        binding.btnMakanan.setOnClickListener { toggleRecyclerView(true) }
        binding.btnMinuman.setOnClickListener { toggleRecyclerView(false) }

        // ✅ Setup tombol tambah menu
        binding.btnTambahMenu.setOnClickListener { tambahMenu() }

        // ✅ Setup pencarian menu
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMenu(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // ✅ Load data menu dari Firebase
        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                makananList.clear()
                minumanList.clear()

                for (dataSnapshot in snapshot.children) {
                    val menuItem = dataSnapshot.getValue(MenuItem::class.java)
                    if (menuItem != null) {
                        when {
                            menuItem.kategori.equals("Makanan", ignoreCase = true) -> makananList.add(menuItem)
                            menuItem.kategori.equals("Minuman", ignoreCase = true) -> minumanList.add(menuItem)
                        }
                    }
                }
                makananAdapter.notifyDataSetChanged()
                minumanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
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
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus menu ini?")
            .setPositiveButton("Ya") { _, _ ->
                hapusMenu(menuItem)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun hapusMenu(menuItem: MenuItem) {
        database.child(menuItem.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Menu berhasil dihapus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menghapus menu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleRecyclerView(showMakanan: Boolean) {
        binding.recyclerViewMakanan.visibility = if (showMakanan) View.VISIBLE else View.GONE
        binding.recyclerViewMinuman.visibility = if (showMakanan) View.GONE else View.VISIBLE

        // ✅ Tambahkan efek perubahan warna tab dan animasi indikator
        binding.btnMakanan.setTextColor(
            ContextCompat.getColor(requireContext(), if (showMakanan) R.color.primaryColor else R.color.grey)
        )
        binding.btnMinuman.setTextColor(
            ContextCompat.getColor(requireContext(), if (showMakanan) R.color.grey else R.color.primaryColor)
        )
        binding.tabIndicator.animate()
            .translationX(if (showMakanan) 0f else binding.tabContainer.width / 2f)
            .setDuration(200)
            .start()
    }

    private fun filterMenu(text: String) {
        val filteredMakanan = makananList.filter { it.nama.contains(text, ignoreCase = true) }
        val filteredMinuman = minumanList.filter { it.nama.contains(text, ignoreCase = true) }

        makananAdapter.updateList(filteredMakanan)
        minumanAdapter.updateList(filteredMinuman)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
