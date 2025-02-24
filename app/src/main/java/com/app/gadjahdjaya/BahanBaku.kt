package com.app.gadjahdjaya.model

import android.os.Parcel
import android.os.Parcelable

data class BahanBaku(
    val id: String = "",
    val nama: String = "",
    val kategori: String = "",
    val stok: Double = 0.0,
    val satuan: String = "",
    val stokMaksimum: Double = 0.0,
    val tanggalMasuk: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        nama = parcel.readString() ?: "",
        kategori = parcel.readString() ?: "",
        stok = parcel.readDouble(),
        stokMaksimum = parcel.readDouble(),
        tanggalMasuk = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nama)
        parcel.writeString(kategori)
        parcel.writeDouble(stok)
        parcel.writeDouble(stokMaksimum)
        parcel.writeString(tanggalMasuk)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BahanBaku> {
        override fun createFromParcel(parcel: Parcel): BahanBaku = BahanBaku(parcel)
        override fun newArray(size: Int): Array<BahanBaku?> = arrayOfNulls(size)
    }
}
