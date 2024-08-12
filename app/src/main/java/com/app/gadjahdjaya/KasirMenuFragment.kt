package com.app.gadjahdjaya

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class KasirMenuFragment : Fragment() {

    private lateinit var recyclerViewMakanan: RecyclerView
    private lateinit var recyclerViewMinuman: RecyclerView
    private lateinit var makananAdapter: KasirMenuAdapter
    private lateinit var minumanAdapter: KasirMenuAdapter
    private lateinit var database: DatabaseReference
    private lateinit var searchEditText: EditText
    private val menuMakananList = mutableListOf<MenuItem>()
    private val menuMinumanList = mutableListOf<MenuItem>()
    private val cartList = mutableListOf<MenuItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kasir_menu, container, false)

        recyclerViewMakanan = view.findViewById(R.id.recyclerView_makanankasir)
        recyclerViewMinuman = view.findViewById(R.id.recyclerView_minumankasir)
        searchEditText = view.findViewById(R.id.searchEditText)

        recyclerViewMakanan.layoutManager = GridLayoutManager(context, 2)
        recyclerViewMinuman.layoutManager = GridLayoutManager(context, 2)

        makananAdapter = KasirMenuAdapter(requireContext(), menuMakananList, object : KasirMenuAdapter.OnItemClickListener {
            override fun onIncreaseQuantity(menuItem: MenuItem) {
                addToCart(menuItem)
                makananAdapter.notifyDataSetChanged()
            }

            override fun onDecreaseQuantity(menuItem: MenuItem) {
                removeFromCart(menuItem)
                makananAdapter.notifyDataSetChanged()
            }
        })
        recyclerViewMakanan.adapter = makananAdapter

        minumanAdapter = KasirMenuAdapter(requireContext(), menuMinumanList, object : KasirMenuAdapter.OnItemClickListener {
            override fun onIncreaseQuantity(menuItem: MenuItem) {
                addToCart(menuItem)
                minumanAdapter.notifyDataSetChanged()
            }

            override fun onDecreaseQuantity(menuItem: MenuItem) {
                removeFromCart(menuItem)
                minumanAdapter.notifyDataSetChanged()
            }
        })
        recyclerViewMinuman.adapter = minumanAdapter

        database = FirebaseDatabase.getInstance().getReference("menuItems")

        view.findViewById<Button>(R.id.btn_makanan).setOnClickListener {
            recyclerViewMakanan.visibility = View.VISIBLE
            recyclerViewMinuman.visibility = View.GONE
        }

        view.findViewById<Button>(R.id.btn_minuman).setOnClickListener {
            recyclerViewMakanan.visibility = View.GONE
            recyclerViewMinuman.visibility = View.VISIBLE
        }

        view.findViewById<Button>(R.id.btn_pembayaran).setOnClickListener {
            navigateToPayment()
        }

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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuMakananList.clear()
                menuMinumanList.clear()
                for (dataSnapshot in snapshot.children) {
                    val menuItemMap = dataSnapshot.value as? Map<String, Any>
                    if (menuItemMap != null) {
                        val menuItem = MenuItem(
                            id = dataSnapshot.key ?: "",
                            kategori = menuItemMap["kategori"] as? String ?: "",
                            gambar = menuItemMap["gambar"] as? String ?: "",
                            nama = menuItemMap["nama"] as? String ?: "",
                            harga = (menuItemMap["harga"] as? Long)?.toInt() ?: 0,
                            jumlah = (menuItemMap["jumlah"] as? Long)?.toInt() ?: 0 // pastikan data dari firebase memiliki jumlah
                        )
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
                // Handle possible errors.
            }
        })
    }

    private fun filterMenu(query: String) {
        val filteredMakananList = menuMakananList.filter { it.nama.contains(query, ignoreCase = true) }
        val filteredMinumanList = menuMinumanList.filter { it.nama.contains(query, ignoreCase = true) }

        makananAdapter.updateData(filteredMakananList)
        minumanAdapter.updateData(filteredMinumanList)
    }

    private fun addToCart(menuItem: MenuItem) {
        val existingItem = cartList.find { it.id == menuItem.id }
        if (existingItem != null) {
            existingItem.jumlah++
        } else {
            menuItem.jumlah = 1
            cartList.add(menuItem)
        }
    }

    private fun removeFromCart(menuItem: MenuItem) {
        val existingItem = cartList.find { it.id == menuItem.id }
        if (existingItem != null) {
            if (existingItem.jumlah > 1) {
                existingItem.jumlah--
            } else {
                cartList.remove(existingItem)
            }
        }
    }

    private fun navigateToPayment() {
        val intent = Intent(requireContext(), PaymentActivity::class.java)
        intent.putParcelableArrayListExtra("cartItems", ArrayList(cartList))
        startActivity(intent)
    }
}
