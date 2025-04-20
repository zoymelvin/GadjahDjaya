package com.app.gadjahdjaya.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.text.TextPaint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.gadjahdjaya.R
import com.app.gadjahdjaya.TransactionAdapter
import com.app.gadjahdjaya.Transaksi
import com.app.gadjahdjaya.Item
import com.app.gadjahdjaya.PenjualanMenuFragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.*
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max


class FragmentKeuangan : Fragment() {

    private lateinit var datePicker: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvProfit: TextView
    private lateinit var barChart: BarChart
    private lateinit var rvTransactions: RecyclerView
    private lateinit var btnAddExpense: Button
    private lateinit var btnExport: Button
    private lateinit var database: DatabaseReference
    private lateinit var btnpenjualanmenu : Button
    private var expandedTransactionId: String? = null


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
        btnpenjualanmenu = view.findViewById(R.id.btnSalesReport)

        btnExport.setOnClickListener { showDateRangePicker() }

        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        // Pastikan hanya satu instance adapter yang digunakan
        transactionAdapter = TransactionAdapter(transactions) { transaction ->
            toggleTransactionDetail(transaction)
        }
        rvTransactions.adapter = transactionAdapter

        // Pilih rentang tanggal dengan kalender modern
        datePicker.setOnClickListener { showModernDateRangePicker() }

        // Load Data dari Firebase (default: transaksi hari ini)
        fetchTransactionsAndExpenses(getTodayDate())

        // Tombol Tambah Pengeluaran
        btnAddExpense.setOnClickListener {
            val bottomSheet = PengeluaranBottomSheet { amount, description, paymentMethod ->
                tambahPengeluaranKeFirebase(amount, description, paymentMethod)
            }
            bottomSheet.show(parentFragmentManager, "PengeluaranBottomSheet")
        }
        btnpenjualanmenu.setOnClickListener {
            val fragment = PenjualanMenuFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) // Gantilah dengan ID container yang benar
                .addToBackStack(null) // Agar bisa kembali ke fragment sebelumnya
                .commit()
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
            fetchTransactionsAndExpenses(startDate, endDate)
        }
    }

    private fun fetchTransactionsAndExpenses(startDate: String, endDate: String? = null) {
        transactions.clear()
        var totalIncome = 0.0
        var totalExpense = 0.0

        database.child("transactions").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (categorySnapshot in snapshot.children) {
                    for (dataSnapshot in categorySnapshot.children) {
                        val transactionId = dataSnapshot.key ?: ""
                        val timestamp = dataSnapshot.child("timestamp").getValue(String::class.java) ?: ""

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
                                    items = itemsList
                                )

                                transactions.add(transaksi)
                                totalIncome += transaksi.amount
                            }
                        }
                    }
                }

                // Ambil data pengeluaran
                database.child("expenses").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(expenseSnapshot: DataSnapshot) {
                        for (expense in expenseSnapshot.children) {
                            val timestamp = expense.child("timestamp").getValue(String::class.java) ?: ""

                            if (timestamp.isNotEmpty() && timestamp.length >= 10) {
                                val expenseDate = timestamp.substring(0, 10)

                                if (expenseDate >= startDate && (endDate == null || expenseDate <= endDate)) {
                                    val transaksi = Transaksi(
                                        id = expense.key ?: "",
                                        amount = -(expense.child("amount").getValue(Double::class.java) ?: 0.0),
                                        timestamp = timestamp,
                                        status = "Pengeluaran",
                                        payment_method = expense.child("payment_method").getValue(String::class.java) ?: "",
                                        description = expense.child("description").getValue(String::class.java) ?: "", // ✅ Ambil deskripsi
                                        items = emptyList() // ✅ Kosong karena pengeluaran tidak memiliki items
                                    )

                                    transactions.add(transaksi)
                                    totalExpense += transaksi.amount
                                }
                            }
                        }

                        // Update UI setelah transaksi dan pengeluaran diambil
                        tvIncome.text = "Rp ${formatCurrency(totalIncome)}"
                        tvExpense.text = "Rp ${formatCurrency(totalExpense)}"
                        tvProfit.text = "Rp ${formatCurrency(totalIncome + totalExpense)}"

                        transactionAdapter.notifyDataSetChanged()
                        loadChart(totalIncome.toInt(), totalExpense.toInt())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseError", "Gagal mengambil data pengeluaran: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Gagal mengambil data transaksi: ${error.message}")
            }
        })
    }



    private fun tambahPengeluaranKeFirebase(amount: Double, description: String, paymentMethod: String) {
        val expenseId = "expense-${System.currentTimeMillis()}"
        val timestamp = getTodayDate() + " " + SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val newExpense = mapOf(
            "id" to expenseId,
            "timestamp" to timestamp,
            "amount" to amount,
            "description" to description,
            "payment_method" to paymentMethod
        )

        database.child("expenses").child(expenseId).setValue(newExpense)
            .addOnSuccessListener {
                Log.d("FirebaseSuccess", "Pengeluaran berhasil ditambahkan")
                fetchTransactionsAndExpenses(getTodayDate()) // Refresh data
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseError", "Gagal menambahkan pengeluaran: ${e.message}")
            }
    }


    private fun loadChart(income: Int, expense: Int) {
        // Create entries
        val entries = listOf(
            BarEntry(0f, income.toFloat()),
            BarEntry(1f, Math.abs(expense.toFloat()))
        )

        // Modern dataset styling
        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.green2), // Green for income
                ContextCompat.getColor(requireContext(), R.color.red)   // Red for expense
            )
            valueTextColor = Color.BLACK  // Changed to black for better visibility
            valueTextSize = 10f           // Larger text size
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "Rp${formatNumber(value.toInt())}" // Compact format
                }
            }
            setDrawValues(true)
            highLightAlpha = 0            // Disable highlight effect
        }

        // Modern bar chart configuration with wider bars
        barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.6f  // Wider bars (original was 0.4f)
            }

            // X-axis styling
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                axisLineColor = Color.LTGRAY
                textColor = Color.DKGRAY
                textSize = 12f
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(listOf("Pemasukan", "Pengeluaran"))
            }

            // Left axis styling
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#EEEEEE")
                axisLineColor = Color.LTGRAY
                textColor = Color.DKGRAY
                textSize = 10f
                axisMinimum = 0f
                axisMaximum = max(income, Math.abs(expense)).toFloat() * 1.3f  // More space for values
            }

            // General chart settings
            axisRight.isEnabled = false
            legend.isEnabled = false
            description.isEnabled = false
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setFitBars(true)
            setPinchZoom(false)
            setDrawGridBackground(false)
            extraBottomOffset = 15f  // More bottom padding

            // Value label positioning
            setExtraOffsets(0f, 0f, 0f, 20f)  // Add space for top values

            // Animation
            animateY(1000, Easing.EaseInOutQuad)
            setBackgroundColor(Color.TRANSPARENT)
        }

        barChart.invalidate()
    }

    // Helper function to format numbers with dots (39.000)
    private fun formatNumber(number: Int): String {
        return NumberFormat.getNumberInstance(Locale.GERMAN).format(number)
    }

    private fun toggleTransactionDetail(transaction: Transaksi) {
        expandedTransactionId = if (expandedTransactionId == transaction.id) null else transaction.id
        transactionAdapter.notifyDataSetChanged() // Perbarui tampilan
    }


    private fun getTodayDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }



    // ✅ Menampilkan kalender untuk memilih rentang tanggal
    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Pilih Rentang Tanggal")
            .build()

        dateRangePicker.show(parentFragmentManager, "DATE_PICKER")

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.format(Date(selection.first))
            val endDate = sdf.format(Date(selection.second))
            generateFinancialReportPdf(startDate, endDate)
        }
    }

    // ✅ Fungsi untuk membuat PDF dengan format tabel
    private fun generateFinancialReportPdf(startDate: String, endDate: String) {
        try {
            val fileName = "Laporan_Keuangan_GadjahDjaya_${startDate}_sampai_${endDate}.pdf"
            val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            val pdfWriter = PdfWriter(FileOutputStream(file))
            val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)

            val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)

            document.add(Paragraph("Laporan Keuangan GadjahDjaya")
                .setFont(boldFont)
                .setFontSize(18f)
                .setTextAlignment(TextAlignment.CENTER))

            document.add(Paragraph("Periode: $startDate - $endDate")
                .setFont(normalFont)
                .setFontSize(13f)
                .setTextAlignment(TextAlignment.CENTER))

            document.add(Paragraph("\nRincian Transaksi").setFont(boldFont).setFontSize(14f))

            // ✅ Warna untuk header tabel
            val headerColor = DeviceGray(0.8f)

// 🔹 Tabel Transaksi
            database.child("transactions").get().addOnSuccessListener { snapshot ->
                for (categorySnapshot in snapshot.children) {
                    for (dataSnapshot in categorySnapshot.children) {
                        val timestamp = dataSnapshot.child("timestamp").getValue(String::class.java) ?: ""
                        val paymentMethod = dataSnapshot.child("payment_method").getValue(String::class.java) ?: ""
                        val grossAmount = dataSnapshot.child("gross_amount").getValue(Double::class.java) ?: 0.0

                        val transactionDate = timestamp.substring(0, 10)
                        if (transactionDate in startDate..endDate) {
                            document.add(
                                Paragraph("\nTanggal: $transactionDate | Metode Pembayaran: $paymentMethod")
                                    .setFont(boldFont).setFontSize(12f)
                            )

                            val table = Table(floatArrayOf(4f, 2f, 3f, 3f)).useAllAvailableWidth()

                            // ✅ Header tabel transaksi dengan warna abu-abu
                            val headers = listOf("Nama Item", "Jumlah", "Harga Satuan", "Harga Total")
                            headers.forEach { headerText ->
                                val headerCell = Cell().add(Paragraph(headerText).setFont(boldFont))
                                    .setBackgroundColor(headerColor) // ✅ Warna abu-abu
                                    .setTextAlignment(TextAlignment.CENTER)
                                table.addHeaderCell(headerCell)
                            }

                            val itemsSnapshot = dataSnapshot.child("items")
                            for (itemSnapshot in itemsSnapshot.children) {
                                val name = itemSnapshot.child("name").getValue(String::class.java) ?: "-"
                                val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                                val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.0
                                val totalPrice = quantity * price

                                table.addCell(Cell().add(Paragraph(name)))
                                table.addCell(Cell().add(Paragraph(quantity.toString())))
                                table.addCell(Cell().add(Paragraph("Rp ${formatCurrency(price)}")))
                                table.addCell(Cell().add(Paragraph("Rp ${formatCurrency(totalPrice)}")))
                            }

                            document.add(table)
                            document.add(
                                Paragraph("Total Transaksi: Rp ${formatCurrency(grossAmount)}")
                                    .setFont(boldFont).setTextAlignment(TextAlignment.RIGHT)
                            )
                        }
                    }
                }

                // ✅ Tabel Pengeluaran
                document.add(Paragraph("\nPengeluaran").setFont(boldFont).setFontSize(14f))

                val expenseTable = Table(floatArrayOf(3f, 3f, 3f)).useAllAvailableWidth()

                // ✅ Header tabel pengeluaran dengan warna abu-abu
                val expenseHeaders = listOf("Tanggal", "Deskripsi Pengeluaran", "Total Pengeluaran")
                expenseHeaders.forEach { headerText ->
                    val headerCell = Cell().add(Paragraph(headerText).setFont(boldFont))
                        .setBackgroundColor(headerColor) // ✅ Warna abu-abu
                        .setTextAlignment(TextAlignment.CENTER)
                    expenseTable.addHeaderCell(headerCell)
                }

                database.child("expenses").get().addOnSuccessListener { expenseSnapshot ->
                    var totalExpense = 0.0
                    for (expense in expenseSnapshot.children) {
                        val date = expense.child("timestamp").getValue(String::class.java)?.substring(0, 10) ?: ""
                        val description = expense.child("description").getValue(String::class.java) ?: "-"
                        val amount = expense.child("amount").getValue(Double::class.java) ?: 0.0

                        expenseTable.addCell(Cell().add(Paragraph(date)))
                        expenseTable.addCell(Cell().add(Paragraph(description)))
                        expenseTable.addCell(Cell().add(Paragraph("Rp ${formatCurrency(amount)}")))

                        totalExpense += amount
                    }

                    document.add(expenseTable)

                    // ✅ Warna untuk header tabel ringkasan keuangan
                    val headerColor = DeviceGray(0.8f)

// ✅ Ringkasan Keuangan
                    document.add(Paragraph("\nRingkasan Keuangan").setFont(boldFont).setFontSize(14f))

                    val summaryTable = Table(floatArrayOf(3f, 3f)).useAllAvailableWidth()

// ✅ Header tabel ringkasan keuangan dengan warna abu-abu
                    val summaryHeaders = listOf("Kategori", "Total")
                    summaryHeaders.forEach { headerText ->
                        val headerCell = Cell().add(Paragraph(headerText).setFont(boldFont))
                            .setBackgroundColor(headerColor) // ✅ Warna abu-abu
                            .setTextAlignment(TextAlignment.CENTER)
                        summaryTable.addHeaderCell(headerCell)
                    }

// ✅ Menghitung Total Pemasukan
                    val totalIncome = snapshot.children.sumOf { categorySnapshot ->
                        categorySnapshot.children.sumOf { dataSnapshot ->
                            dataSnapshot.child("gross_amount").getValue(Double::class.java) ?: 0.0
                        }
                    }
                    summaryTable.addCell(Cell().add(Paragraph("Total Pemasukan")))
                    summaryTable.addCell(Cell().add(Paragraph("Rp ${formatCurrency(totalIncome)}")))

                    summaryTable.addCell(Cell().add(Paragraph("Total Pengeluaran")))
                    summaryTable.addCell(Cell().add(Paragraph("Rp ${formatCurrency(totalExpense)}")))

                    summaryTable.addCell(Cell().add(Paragraph("Laba Bersih")))
                    summaryTable.addCell(Cell().add(Paragraph("Rp ${formatCurrency(totalIncome - totalExpense)}")))

                    document.add(summaryTable)
                    document.close()

                    sharePdf(file)
                }
            }
        } catch (e: Exception) {
            Log.e("PDF_ERROR", "Gagal membuat PDF: ${e.message}")
        }
    }

    // ✅ Fungsi untuk format angka dengan titik
    private fun formatCurrency(amount: Double): String {
        val formatter = DecimalFormat("#,###")
        return formatter.format(amount)
    }

    // ✅ Fungsi berbagi PDF
    private fun sharePdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Bagikan Laporan Keuangan"))
    }



}