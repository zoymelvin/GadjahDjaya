package com.app.gadjahdjaya

import android.os.Parcel
import android.os.Parcelable

data class MenuItem(
    var id: String = "",
    var kategori: String = "",
    var gambar: String = "",
    var nama: String = "",
    var harga: Int = 0,
    var jumlah: Int = 1,
    var timestamp: Long = 0L
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(kategori)
        parcel.writeString(gambar)
        parcel.writeString(nama)
        parcel.writeInt(harga)
        parcel.writeInt(jumlah)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MenuItem> {
        override fun createFromParcel(parcel: Parcel): MenuItem {
            return MenuItem(parcel)
        }

        override fun newArray(size: Int): Array<MenuItem?> {
            return arrayOfNulls(size)
        }
    }
}
