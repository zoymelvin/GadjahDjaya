package com.app.gadjahdjaya

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.model.MenuItem
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

data class Transaction(
    val id: String = "",
    val items: List<MenuItem> = listOf(),
    val totalPrice: Int = 0,
    val timestamp: String = "",
    val paymentMethod: String = ""
)

class KeuanganFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var database: DatabaseReference
    private val transactionList = mutableListOf<Transaction>()
    private var filteredTransactionList = mutableListOf<Any>()
    private lateinit var spinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_keuangan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView_transactions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(filteredTransactionList) { transaction ->
            showTransactionDetails(transaction as Transaction)
        }
        recyclerView.adapter = transactionAdapter

        spinner = view.findViewById(R.id.spinner_filter)
        setupSpinner()

        fetchTransactions()
    }

    private fun setupSpinner() {
        val options = arrayOf("Semua", "Hari ini", "7 hari terakhir", "1 bulan terakhir")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterTransactions(options[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchTransactions() {
        database = FirebaseDatabase.getInstance().getReference("transactions")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionList.clear()
                for (dataSnapshot in snapshot.children) {
                    val transaction = dataSnapshot.getValue<Transaction>()
                    if (transaction != null) {
                        transactionList.add(transaction)
                    }
                }
                // Urutkan transaksi berdasarkan timestamp dari terbaru ke terlama
                transactionList.sortByDescending { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.timestamp) }
                filterTransactions(spinner.selectedItem.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })
    }

    private fun filterTransactions(option: String) {
        val now = Date()
        val filteredList = ArrayList<Any>()
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())


        for (transaction in transactionList) {
            val transactionDate = format.parse(transaction.timestamp)
            if (transactionDate != null) {
                when (option) {
                    "Semua" -> filteredList.add(transaction)
                    "Hari ini" -> {
                        val calendar = Calendar.getInstance()
                        calendar.time = now
                        val today = calendar.get(Calendar.DAY_OF_YEAR)
                        calendar.time = transactionDate
                        if (today == calendar.get(Calendar.DAY_OF_YEAR)) {
                            filteredList.add(transaction)
                        }
                    }
                    "7 hari terakhir" -> {
                        val diff = now.time - transactionDate.time
                        val daysDiff = diff / (1000 * 60 * 60 * 24)
                        if (daysDiff <= 7) {
                            filteredList.add(transaction)
                        }
                    }
                    "1 bulan terakhir" -> {
                        val diff = now.time - transactionDate.time
                        val daysDiff = diff / (1000 * 60 * 60 * 24)
                        if (daysDiff <= 30) {
                            filteredList.add(transaction)
                        }
                    }
                }
            }
        }
        filteredTransactionList.clear()
        filteredTransactionList.addAll(filteredList)
        transactionAdapter.notifyDataSetChanged()
    }

    private fun showTransactionDetails(transaction: Transaction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_details, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Transaction Details")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .create()

        val recyclerViewItems = dialogView.findViewById<RecyclerView>(R.id.recyclerView_items)
        recyclerViewItems.layoutManager = LinearLayoutManager(context)
        recyclerViewItems.adapter = MenuItemAdapter(transaction.items)

        val totalTransactionText = dialogView.findViewById<TextView>(R.id.total_transaction)
        totalTransactionText.text = "Total Transaksi: Rp ${Utils.formatCurrency(transaction.totalPrice)}"

        dialog.show()
    }
}
