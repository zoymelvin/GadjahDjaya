package com.app.gadjahdjaya.model

import android.os.Parcel
import android.os.Parcelable

// ✅ **Data class untuk menyimpan bahan baku yang dibutuhkan oleh menu**
data class BahanBakuDibutuhkan(
    var idBahan: String = "",  // **✅ ID bahan baku (agar sesuai dengan database Firebase)**
    var namaBahan: String = "",  // **Nama bahan baku (contoh: Garam, Nasi)**
    var jumlah: Double = 0.0,    // **Jumlah yang dibutuhkan**
    var satuan: String = ""      // **Satuan bahan baku (gram, pcs, ml, dll.)**
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idBahan)
        parcel.writeString(namaBahan)
        parcel.writeDouble(jumlah)
        parcel.writeString(satuan)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BahanBakuDibutuhkan> {
        override fun createFromParcel(parcel: Parcel): BahanBakuDibutuhkan = BahanBakuDibutuhkan(parcel)
        override fun newArray(size: Int): Array<BahanBakuDibutuhkan?> = arrayOfNulls(size)
    }
}

// ✅ **Data class untuk menyimpan informasi menu**
data class MenuItem(
    var id: String = "",
    var kategori: String = "",
    var gambar: String = "",
    var nama: String = "",
    var harga: Int = 0,
    var jumlah: Int = 1,
    var timestamp: Long = 0L,
    var deskripsi: String = "",
    var bahanBakuDibutuhkan: List<BahanBakuDibutuhkan> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.createTypedArrayList(BahanBakuDibutuhkan) ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(kategori)
        parcel.writeString(gambar)
        parcel.writeString(nama)
        parcel.writeInt(harga)
        parcel.writeInt(jumlah) // ✅ **Tambahan: Menyimpan jumlah ke parcel**
        parcel.writeLong(timestamp)
        parcel.writeString(deskripsi)
        parcel.writeTypedList(bahanBakuDibutuhkan)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MenuItem> {
        override fun createFromParcel(parcel: Parcel): MenuItem = MenuItem(parcel)
        override fun newArray(size: Int): Array<MenuItem?> = arrayOfNulls(size)
    }
}
