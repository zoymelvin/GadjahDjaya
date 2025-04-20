package com.app.gadjahdjaya.ui.stokbahan

import DatePickerFragment
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.gadjahdjaya.databinding.FragmentTotalStockBinding
import com.app.gadjahdjaya.export.CsvExporter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class FragmentTotalStock : Fragment() {

    private var _binding: FragmentTotalStockBinding? = null
    private val binding get() = _binding!!
    private lateinit var pieChart: PieChart
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("log_stok")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTotalStockBinding.inflate(inflater, container, false)
        pieChart = binding.pieChart

        // Tampilkan data hari ini saat pertama kali membuka halaman
        val currentDate = Calendar.getInstance().time
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate)
        fetchDataFromFirebase(today, today)

        // Panggil DatePicker saat ikon kalender diklik
        binding.btnDatePicker.setOnClickListener {
            val datePicker = DatePickerFragment { startDate, endDate ->
                fetchDataFromFirebase(startDate, endDate)
            }
            datePicker.showDatePicker(requireActivity())
        }
        // Tambahkan event listener untuk button tambah bahan
        binding.btnAddStock.setOnClickListener {
            val dialog = DialogTambahBahan()
            dialog.show(childFragmentManager, "DialogTambahBahan")
        }
        // Tambahkan event listener untuk button unduh CSV
        binding.btnUnduhCsv.setOnClickListener {
            val datePicker = DatePickerFragment { startDate, endDate ->
                val csvExporter = CsvExporter(requireContext()) // Buat instance CsvExporter
                csvExporter.exportCsv(startDate, endDate) // Panggil metode dengan instance
            }
            datePicker.showDatePicker(requireActivity())
        }

        return binding.root
    }

    private fun fetchDataFromFirebase(startDate: String, endDate: String) {
        val dateList = getDateRange(startDate, endDate)
        val dataMap = mutableMapOf<String, Float>()

        for (date in dateList) {
            val dateRef = databaseRef.child(date).child("pengeluaran")
            dateRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (itemSnapshot in snapshot.children) {
                        val kategori = itemSnapshot.child("kategori").getValue(String::class.java)?.lowercase() ?: "lainnya"
                        val itemValue = itemSnapshot.child("total_pemakaian").getValue(Int::class.java)?.toFloat() ?: 0f

                        dataMap[kategori] = dataMap.getOrDefault(kategori, 0f) + itemValue
                    }

                    // Hitung total pemakaian untuk konversi ke persentase
                    val totalPemakaian = dataMap.values.sum()
                    val dataPersen = if (totalPemakaian > 0) {
                        dataMap.mapValues { (it.value / totalPemakaian) * 100 }
                    } else {
                        dataMap // Jika total 0, tetap gunakan data asli
                    }

                    updatePieChart(dataPersen)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }



    private fun getDateRange(startDate: String, endDate: String): List<String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startCal = Calendar.getInstance().apply { time = sdf.parse(startDate)!! }
        val endCal = Calendar.getInstance().apply { time = sdf.parse(endDate)!! }

        val dateList = mutableListOf<String>()
        while (!startCal.after(endCal)) {
            dateList.add(sdf.format(startCal.time))
            startCal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return dateList
    }

    private fun updatePieChart(dataMap: Map<String, Float>) {
        val entries = ArrayList<PieEntry>()
        for ((key, value) in dataMap) {
            entries.add(PieEntry(value, "")) // Empty label to hide category names
        }

        // Original color mapping for each category
        val colors = mapOf(
            "bumbu halus" to Color.parseColor("#4CAF50"), // Green
            "kecap" to Color.parseColor("#5C4125"),      // Brown
            "minyak goreng" to Color.parseColor("#F5F05F"), // Yellow
            "sayur" to Color.parseColor("#34D399"),      // Light Green
            "mie" to Color.parseColor("#F9932C"),       // Orange
            "protein" to Color.parseColor("#FF3D00"),   // Red
            "lainnya" to Color.parseColor("#2563EB")    // Blue
        )

        val dataSet = PieDataSet(entries, "").apply {
            // Assign colors based on category
            this.colors = entries.mapIndexed { index, _ ->
                val category = dataMap.keys.elementAt(index).lowercase()
                colors[category] ?: Color.GRAY
            }

            valueTextSize = 12f
            setDrawValues(true)
            valueFormatter = PercentFormatter(pieChart)
            valueTextColor = Color.WHITE
            valueTypeface = Typeface.DEFAULT_BOLD
            sliceSpace = 2f
            selectionShift = 5f
            setDrawIcons(false)
        }

        // Pie chart configuration
        pieChart.apply {
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 45f
            setTransparentCircleRadius(50f)
            setTransparentCircleColor(Color.parseColor("#80FFFFFF"))
            setTransparentCircleAlpha(100)
            setDrawEntryLabels(false)
            legend.isEnabled = false
            description.isEnabled = false
            rotationAngle = 0f
            isRotationEnabled = true
            setUsePercentValues(true)
            animateY(1000, Easing.EaseInOutQuad)
            setDrawCenterText(true)
            centerText = "Total\n100%"
            setCenterTextSize(16f)
            setCenterTextColor(Color.BLACK)
            setCenterTextTypeface(Typeface.DEFAULT_BOLD)
            elevation = 8f
            setExtraOffsets(5f, 10f, 5f, 5f)
        }

        val pieData = PieData(dataSet).apply {
            setValueTextSize(12f)
        }

        pieChart.data = pieData
        pieChart.invalidate()

        // Update category values with original colors
        binding.apply {
            listOf(
                Triple(bumbu, "bumbu halus", "#060606"),
                Triple(kecap, "kecap", "#060606"),
                Triple(goreng, "minyak goreng", "#060606"),
                Triple(sayur, "sayur", "#060606"),
                Triple(mie, "mie", "#060606"),
                Triple(protein, "protein", "#060606"),
                Triple(lain, "lainnya", "#060606")
            ).forEach { (textView, category, colorHex) ->
                // Format percentage with 1 decimal place
                val percentage = dataMap.getOrDefault(category, 0f)
                textView.text = "%.1f%%".format(percentage).replace(",", ".") // Ensure dot as decimal separator
                textView.setTextColor(Color.parseColor(colorHex))
                textView.typeface = Typeface.DEFAULT_BOLD
                textView.textSize = 12f
            }
        }

        fetchStockSummary()
    }

    private fun fetchStockSummary() {
        val stockRef = FirebaseDatabase.getInstance().getReference("stok")
        stockRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalItem = 0
                val kategoriSet = mutableSetOf<String>()
                var stokMenipis = 0

                for (itemSnapshot in snapshot.children) {
                    val kategori = itemSnapshot.child("kategori").getValue(String::class.java) ?: "Lainnya"
                    val stok = itemSnapshot.child("stok").getValue(Int::class.java) ?: 0
                    val kapasitas = itemSnapshot.child("kapasitas").getValue(Int::class.java) ?: 100

                    totalItem++
                    kategoriSet.add(kategori)

                    // Cek apakah stok menipis (di bawah 20% dari kapasitas)
                    if (stok.toFloat() / kapasitas * 100 < 20) {
                        stokMenipis++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
