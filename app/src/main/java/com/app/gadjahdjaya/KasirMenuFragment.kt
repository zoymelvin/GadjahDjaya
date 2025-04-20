package com.app.gadjahdjaya

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.model.MenuItem
import com.app.gadjahdjaya.ui.payment.PaymentActivity
import com.google.firebase.database.*

class KasirMenuFragment : Fragment() {

    private lateinit var recyclerViewMakanan: RecyclerView
    private lateinit var recyclerViewMinuman: RecyclerView
    private lateinit var makananAdapter: KasirMenuAdapter
    private lateinit var minumanAdapter: KasirMenuAdapter
    private lateinit var database: DatabaseReference
    private lateinit var searchEditText: EditText
    private lateinit var btnMakanan: TextView
    private lateinit var btnMinuman: TextView
    private lateinit var btnCheckout: Button
    private lateinit var tabIndicator: View

    private val menuMakananList = mutableListOf<MenuItem>()
    private val menuMinumanList = mutableListOf<MenuItem>()
    private val cartMap = mutableMapOf<String, MenuItem>() // ✅ Menggunakan HashMap untuk menghindari duplikasi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kasir_menu, container, false)

        // Inisialisasi UI
        recyclerViewMakanan = view.findViewById(R.id.recyclerView_makanan)
        recyclerViewMinuman = view.findViewById(R.id.recyclerView_minuman)
        searchEditText = view.findViewById(R.id.searchEditText)
        btnMakanan = view.findViewById(R.id.btn_makanan)
        btnMinuman = view.findViewById(R.id.btn_minuman)
        btnCheckout = view.findViewById(R.id.btn_buat_pesanan)
        tabIndicator = view.findViewById(R.id.tab_indicator)

        // Atur layout Grid
        recyclerViewMakanan.layoutManager = GridLayoutManager(context, 1)
        recyclerViewMinuman.layoutManager = GridLayoutManager(context, 1)

        // Adapter untuk makanan
        makananAdapter = KasirMenuAdapter(requireContext(), menuMakananList, object : KasirMenuAdapter.OnItemClickListener {
            override fun onIncreaseQuantity(menuItem: MenuItem) {
                updateCart(menuItem.id, 1)
            }

            override fun onDecreaseQuantity(menuItem: MenuItem) {
                updateCart(menuItem.id, -1)
            }
        })
        recyclerViewMakanan.adapter = makananAdapter

        // Adapter untuk minuman
        minumanAdapter = KasirMenuAdapter(requireContext(), menuMinumanList, object : KasirMenuAdapter.OnItemClickListener {
            override fun onIncreaseQuantity(menuItem: MenuItem) {
                updateCart(menuItem.id, 1)
            }

            override fun onDecreaseQuantity(menuItem: MenuItem) {
                updateCart(menuItem.id, -1)
            }
        })
        recyclerViewMinuman.adapter = minumanAdapter

        // Firebase Database
        database = FirebaseDatabase.getInstance().getReference("menuItems")

        // Tombol kategori makanan
        btnMakanan.setOnClickListener {
            recyclerViewMakanan.visibility = View.VISIBLE
            recyclerViewMinuman.visibility = View.GONE
            btnMakanan.setTextColor(resources.getColor(R.color.blue_primary))
            btnMinuman.setTextColor(resources.getColor(R.color.grey2))

            tabIndicator.animate().x(0f).setDuration(200).start()
        }

        // Tombol kategori minuman
        btnMinuman.setOnClickListener {
            recyclerViewMakanan.visibility = View.GONE
            recyclerViewMinuman.visibility = View.VISIBLE
            btnMakanan.setTextColor(resources.getColor(R.color.grey2))
            btnMinuman.setTextColor(resources.getColor(R.color.blue_primary))

            val tabWidth = btnMakanan.width
            tabIndicator.animate().x(tabWidth.toFloat()).setDuration(200).start()
        }

        // Tombol checkout
        btnCheckout.setOnClickListener {
            navigateToPayment()
        }

        // Pencarian menu
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMenu(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchDataFromDatabase()
    }

    private fun fetchDataFromDatabase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuMakananList.clear()
                menuMinumanList.clear()

                for (dataSnapshot in snapshot.children) {
                    val menuItem = dataSnapshot.getValue(MenuItem::class.java)
                    if (menuItem != null) {
                        menuItem.jumlah = 0 // Pastikan default jumlah adalah 0
                        if (menuItem.kategori == "Makanan") {
                            menuMakananList.add(menuItem)
                        } else if (menuItem.kategori == "Minuman") {
                            menuMinumanList.add(menuItem)
                        }
                    }
                }
                makananAdapter.notifyDataSetChanged()
                minumanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Tangani error
            }
        })
    }

    private fun filterMenu(query: String) {
        val filteredMakananList = menuMakananList.filter { it.nama.contains(query, ignoreCase = true) }
        val filteredMinumanList = menuMinumanList.filter { it.nama.contains(query, ignoreCase = true) }

        makananAdapter.updateData(filteredMakananList)
        minumanAdapter.updateData(filteredMinumanList)
    }

    private fun updateCart(menuId: String, change: Int) {
        val menuItem = (menuMakananList.find { it.id == menuId } ?: menuMinumanList.find { it.id == menuId })?.copy()

        if (menuItem != null) {
            val existingItem = cartMap[menuId]

            if (existingItem != null) {
                existingItem.jumlah += change
                if (existingItem.jumlah <= 0) {
                    cartMap.remove(menuId)
                }
            } else if (change > 0) {
                menuItem.jumlah = 1
                cartMap[menuId] = menuItem
            }

            Log.d("KasirMenu", "Cart: ${cartMap.size} items")

            makananAdapter.notifyDataSetChanged()
            minumanAdapter.notifyDataSetChanged()
        }
    }

    private fun navigateToPayment() {
        if (cartMap.isEmpty()) {
            showErrorToast("Silakan tambahkan menu terlebih dahulu")
            return
        }

        val cartList = ArrayList(cartMap.values)

        val intent = Intent(requireContext(), PaymentActivity::class.java)
        intent.putParcelableArrayListExtra("cartItems", cartList)
        startActivity(intent)
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
