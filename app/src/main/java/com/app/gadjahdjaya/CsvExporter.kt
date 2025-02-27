package com.app.gadjahdjaya.export

import DatePickerFragment
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CsvExporter(private val context: Context) {
    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("log_stok")

    fun exportCsv(startDate: String, endDate: String) {
        val dateList = getDateRange(startDate, endDate)
        val csvFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "pemasukan_pengeluaran_${startDate}_to_${endDate}.csv")

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

                        // Ambil data pengeluaran
                        val pengeluaranSnapshot = dateRef.child("pengeluaran")
                        for (item in pengeluaranSnapshot.children) {
                            val kategori = item.child("kategori").getValue(String::class.java) ?: "Tidak Diketahui"
                            val bahan = item.child("nama").getValue(String::class.java) ?: "Tidak Diketahui"
                            val jumlah = item.child("total_pemakaian").getValue(Int::class.java) ?: 0
                            val satuan = item.child("satuan").getValue(String::class.java) ?: "Tidak Diketahui"

                            writer.append("$date,$kategori,$bahan,$jumlah,$satuan,Pengeluaran\n")
                        }
                    }

                    writer.flush()
                    writer.close()

                    Toast.makeText(context, "CSV berhasil disimpan di: ${csvFile.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "Gagal menyimpan CSV", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
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

