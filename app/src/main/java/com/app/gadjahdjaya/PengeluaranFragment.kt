package com.app.gadjahdjaya.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.Item
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.TransactionAdapter
import com.app.gadjahdjaya.Transaksi
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FragmentPengeluaranKeuangan : Fragment() {

    private lateinit var datePicker: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvProfit: TextView
    private lateinit var barChart: BarChart
    private lateinit var rvTransactions: RecyclerView
    private lateinit var btnAddExpense: Button
    private lateinit var btnExport: Button
    private lateinit var database: DatabaseReference

    private lateinit var transactionAdapter: TransactionAdapter
    private var transactions: MutableList<Transaksi> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_keuangan, container, false)

        // Inisialisasi UI
        datePicker = view.findViewById(R.id.datePicker)
        tvIncome = view.findViewById(R.id.tvIncome)
        tvExpense = view.findViewById(R.id.tvExpense)
        tvProfit = view.findViewById(R.id.tvProfit)
        barChart = view.findViewById(R.id.barChart)
        rvTransactions = view.findViewById(R.id.rvTransactions)
        btnAddExpense = view.findViewById(R.id.btnAddExpense)
        btnExport = view.findViewById(R.id.btnExport)
        database = FirebaseDatabase.getInstance().reference

        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        transactionAdapter = TransactionAdapter(transactions) {}
        rvTransactions.adapter = transactionAdapter

        // Pilih rentang tanggal dengan kalender modern
        datePicker.setOnClickListener { showModernDateRangePicker() }

        // Load Data dari Firebase (default: transaksi hari ini)
        fetchTransactionsFromFirebase(getTodayDate())

        // Tombol Tambah Pengeluaran
        btnAddExpense.setOnClickListener {
            val bottomSheet = PengeluaranBottomSheet { amount, description, paymentMethod ->
                tambahPengeluaranKeFirebase(amount, description, paymentMethod)
            }
            bottomSheet.show(parentFragmentManager, "PengeluaranBottomSheet")
        }

        return view
    }

    private fun showModernDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .build()

        dateRangePicker.show(parentFragmentManager, "DATE_PICKER")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.format(Date(selection.first))
            val endDate = sdf.format(Date(selection.second))
            datePicker.text = "$startDate - $endDate"
            fetchTransactionsFromFirebase(startDate, endDate)
        }
    }

    private fun fetchTransactionsFromFirebase(startDate: String, endDate: String? = null) {
        database.child("transactions").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                transactions.clear()
                var totalIncome = 0.0
                var totalExpense = 0.0

                Log.d("FirebaseDebug", "Total transaksi ditemukan: ${snapshot.childrenCount}")

                for (categorySnapshot in snapshot.children) { // Loop kategori offline/online
                    for (dataSnapshot in categorySnapshot.children) { // Loop transaksi di dalamnya
                        val transactionId = dataSnapshot.key ?: ""
                        val timestamp = dataSnapshot.child("timestamp").getValue(String::class.java) ?: ""

                        Log.d("FirebaseDebug", "Transaksi ditemukan: ID=$transactionId, Timestamp=$timestamp")

                        if (timestamp.isNotEmpty() && timestamp.length >= 10) {
                            val transactionDate = timestamp.substring(0, 10)

                            if (transactionDate >= startDate && (endDate == null || transactionDate <= endDate)) {
                                val itemsList = mutableListOf<Item>()
                                val itemsSnapshot = dataSnapshot.child("items")
                                for (itemSnapshot in itemsSnapshot.children) {
                                    val item = itemSnapshot.getValue(Item::class.java)
                                    if (item != null) {
                                        itemsList.add(item)
                                    }
                                }

                                val transaksi = Transaksi(
                                    id = transactionId,
                                    amount = dataSnapshot.child("gross_amount").getValue(Double::class.java) ?: 0.0,
                                    timestamp = timestamp,
                                    status = dataSnapshot.child("status").getValue(String::class.java) ?: "",
                                    payment_method = dataSnapshot.child("payment_method").getValue(String::class.java) ?: "",
                                    items = itemsList // ✅ Sekarang items diambil dari Firebase!
                                )


                                transactions.add(transaksi)

                                if (transaksi.amount > 0) {
                                    totalIncome += transaksi.amount
                                } else {
                                    totalExpense += transaksi.amount
                                }
                            }
                        }
                    }
                }

                Log.d("FirebaseDebug", "Total transaksi setelah filter: ${transactions.size}")

                tvIncome.text = "Rp ${formatCurrency(totalIncome)}"
                tvExpense.text = "Rp ${formatCurrency(totalExpense)}"
                tvProfit.text = "Rp ${formatCurrency(totalIncome - totalExpense)}"

                transactionAdapter.notifyDataSetChanged()
                loadChart(totalIncome.toInt(), totalExpense.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Gagal mengambil data transaksi: ${error.message}")
            }
        })
    }

    private fun tambahPengeluaranKeFirebase(amount: Double, description: String, paymentMethod: String) {
        val database = FirebaseDatabase.getInstance().reference.child("expenses")

        val expenseId = "expense-${System.currentTimeMillis()}"
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val pengeluaran = mapOf(
            "id" to expenseId,
            "timestamp" to timestamp,
            "amount" to amount,
            "description" to description,
            "payment_method" to paymentMethod
        )

        database.child(expenseId).setValue(pengeluaran)
            .addOnSuccessListener {
                Log.d("Firebase", "Pengeluaran berhasil disimpan")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Gagal menyimpan pengeluaran", it)
            }
    }

    private fun loadChart(income: Int, expense: Int) {
        val entries = listOf(
            BarEntry(0f, income.toFloat()),
            BarEntry(1f, expense.toFloat())
        )
        val dataSet = BarDataSet(entries, "Keuangan").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
        }

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate()

        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(listOf("Pemasukan", "Pengeluaran"))
        }
    }

    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun formatCurrency(amount: Double): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)
    }
}
