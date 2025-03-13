package com.app.gadjahdjaya

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class PenjualanMenuFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MenuSalesAdapter
    private lateinit var datePicker: EditText
    private lateinit var searchBar: EditText
    private val database = FirebaseDatabase.getInstance().reference
    private val menuSalesList = mutableMapOf<String, MenuSalesData>()
    private val menuImages = mutableMapOf<String, String>()
    private val processedTransactions = mutableSetOf<String>() // ✅ Set untuk mencegah duplikasi
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null
    private var todayDate: String = dateFormat.format(Date())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_penjualan_menu, container, false)
        recyclerView = view.findViewById(R.id.rvTransactions)
        datePicker = view.findViewById(R.id.datePicker)
        searchBar = view.findViewById(R.id.etSearch)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MenuSalesAdapter(ArrayList())
        recyclerView.adapter = adapter

        fetchMenuImages()
        fetchTransactions(firstLoad = true)

        datePicker.setOnClickListener {
            showModernDatePicker()
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterSearch(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    private fun fetchMenuImages() {
        database.child("menuItems").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuImages.clear()
                for (menuSnapshot in snapshot.children) {
                    val menuId = menuSnapshot.child("id").value.toString()
                    val imageUrl = menuSnapshot.child("gambar").value.toString()
                    menuImages[menuId] = imageUrl
                }
                fetchTransactions(firstLoad = true)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchTransactions(firstLoad: Boolean) {
        val transactionPaths = listOf("transactions/offline", "transactions/online")

        if (firstLoad) {
            menuSalesList.clear()
            processedTransactions.clear()
        }

        for (path in transactionPaths) {
            database.child(path).addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    processTransaction(snapshot)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    processTransaction(snapshot)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun processTransaction(snapshot: DataSnapshot) {
        val transactionId = snapshot.key ?: return
        if (processedTransactions.contains(transactionId)) return // ✅ Cegah duplikasi

        val transactionDate = snapshot.child("timestamp").value.toString().split(" ")[0]

        // Jika belum memilih tanggal, tampilkan hanya transaksi hari ini
        if (selectedStartDate == null && selectedEndDate == null) {
            if (transactionDate != todayDate) return
        } else if (transactionDate !in selectedStartDate!!..selectedEndDate!!) {
            return
        }

        val itemsSnapshot = snapshot.child("items")
        for (itemSnapshot in itemsSnapshot.children) {
            val menuId = itemSnapshot.child("id").value.toString()
            val name = itemSnapshot.child("name").value.toString()
            val price = itemSnapshot.child("price").value.toString().toInt()
            val quantity = itemSnapshot.child("quantity").value.toString().toInt()
            val revenue = price * quantity

            menuSalesList[menuId] = menuSalesList[menuId]?.copy(
                quantity = menuSalesList[menuId]?.quantity?.plus(quantity) ?: quantity,
                revenue = menuSalesList[menuId]?.revenue?.plus(revenue) ?: revenue
            ) ?: MenuSalesData(
                id = menuId,
                menuName = name,
                quantity = quantity,
                revenue = revenue,
                imageUrl = menuImages[menuId] ?: "",
                date = transactionDate
            )
        }

        processedTransactions.add(transactionId) // ✅ Tandai transaksi sebagai sudah diproses
        adapter.updateList(menuSalesList.values.toList())
    }

    private fun showModernDatePicker() {
        val datePickerDialog = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .build()

        datePickerDialog.show(parentFragmentManager, "DATE_PICKER")

        datePickerDialog.addOnPositiveButtonClickListener { selection ->
            selectedStartDate = selection.first?.let { dateFormat.format(Date(it)) }
            selectedEndDate = selection.second?.let { dateFormat.format(Date(it)) }
            if (selectedStartDate != null && selectedEndDate != null) {
                datePicker.setText("$selectedStartDate → $selectedEndDate")
                fetchTransactions(firstLoad = false)
            }
        }
    }

    private fun filterSearch(query: String) {
        val filteredList = menuSalesList.values.filter { it.menuName.contains(query, ignoreCase = true) }
        adapter.updateList(filteredList)
    }
}
