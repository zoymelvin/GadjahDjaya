package com.app.gadjahdjaya.export

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CsvExporter(private val context: Context) {
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("log_stok")

    fun exportCsv(startDate: String, endDate: String) {
        Log.d("CsvExporter", "exportCsv dipanggil dengan tanggal: $startDate - $endDate")

        val dateList = getDateRange(startDate, endDate)
        if (dateList.isEmpty()) {
            Log.d("CsvExporter", "Date range kosong, tidak ada data yang diekspor")
            return
        }

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val csvFile = File(downloadsDir, "Pemasukan_BahanBaku_${startDate}_to_${endDate}.csv")


        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val writer = FileWriter(csvFile)
                    writer.append("Tanggal,Kategori,Bahan,Jumlah,Satuan,Tipe Transaksi\n")

                    for (date in dateList) {
                        val dateRef = snapshot.child(date)

                        if (!dateRef.exists()) continue

                        // Ambil data pemasukan
                        val pemasukanSnapshot = dateRef.child("pemasukan")
                        for (item in pemasukanSnapshot.children) {
                            val kategori = item.child("kategori").getValue(String::class.java) ?: "Tidak Diketahui"
                            val bahan = item.child("nama").getValue(String::class.java) ?: "Tidak Diketahui"
                            val jumlah = item.child("jumlah").getValue(Int::class.java) ?: 0
                            val satuan = item.child("satuan").getValue(String::class.java) ?: "Tidak Diketahui"

                            writer.append("$date,$kategori,$bahan,$jumlah,$satuan,Pemasukan\n")
                        }
                    }

                    writer.flush()
                    writer.close()

                    Log.d("CsvExporter", "CSV berhasil dibuat di: ${csvFile.absolutePath}")
                    Toast.makeText(context, "CSV berhasil disimpan di: ${csvFile.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    Log.e("CsvExporter", "Gagal menyimpan CSV: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(context, "Gagal menyimpan CSV", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CsvExporter", "Gagal mengambil data dari Firebase: ${error.message}")
                Toast.makeText(context, "Gagal mengambil data dari Firebase", Toast.LENGTH_LONG).show()
            }
        })
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
}