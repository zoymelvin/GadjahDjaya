# GadjahDjaya

GadjahDjaya adalah aplikasi Point of Sale (POS) berbasis Android yang dirancang untuk membantu mengelola operasi bisnis Rumah makan GadjahDjaya. Aplikasi ini mencakup fungsionalitas untuk manajemen menu, pelacakan inventaris bahan baku, sistem kasir dengan integrasi pembayaran, dan pelaporan keuangan dasar.

## Fitur Utama

Berdasarkan struktur project dan dependensi, berikut adalah fitur-fitur utama aplikasi:

  * **Autentikasi Pengguna:** Sistem login dan registrasi yang aman menggunakan **Firebase Authentication**.
  * **Manajemen Menu:** Kemampuan untuk menambah, melihat, mengedit, dan menghapus (CRUD) item menu.
  * **Manajemen Inventaris:** Pelacakan stok bahan baku, termasuk penyesuaian stok dan peringatan stok menipis.
  * **Sistem Kasir (POS):** Antarmuka kasir untuk memproses pesanan pelanggan.
  * **Integrasi Pembayaran:** Menerima pembayaran digital melalui gateway **Midtrans** (mode Sandbox).
  * **Manajemen Keuangan:** Pencatatan pemasukan (penjualan) dan pengeluaran operasional.
  * **Visualisasi Data:** Menampilkan laporan keuangan dan penjualan dalam bentuk grafik menggunakan **MPAndroidChart**.
  * **Ekspor Laporan:** Kemampuan untuk menghasilkan laporan, kemungkinan dalam format PDF (menggunakan **iText7**) atau CSV.

## Teknologi yang Digunakan

Project ini dibangun menggunakan tumpukan teknologi Android modern:

  * **Bahasa:** [Kotlin](https://kotlinlang.org/)
  * **Arsitektur:**
      * Menggunakan **View Binding** untuk interaksi UI.
      * **Android Navigation Component** untuk mengelola alur navigasi antar fragmen.
      * **Kotlin Coroutines** untuk manajemen asynchronous.
  * **UI:**
      * **AndroidX** (Core, AppCompat, ConstraintLayout)
      * **Material Design**
  * **Backend & Database:**
      * **Firebase (Platform):**
          * **Firebase Authentication** (Autentikasi)
          * **Firebase Realtime Database** (Database NoSQL real-time)
          * **Firebase Storage** (Penyimpanan file, misal gambar menu)
          * **Firebase Analytics** (Analisis pengguna)
          * **Firebase Functions** (Backend logic)
          * **Firebase AppCheck** (Keamanan)
  * **Pembayaran:**
      * **Midtrans UIKIt** (Payment Gateway)
  * **Visualisasi Data:**
      * **MPAndroidChart** (Grafik pai, batang, dll.)
  * **Utilities:**
      * **Glide** & **Picasso** (Image loading)
      * **iText7** (Pembuatan file PDF)
      * **Gson** (Serialisasi/Deserialisasi JSON)

## Prasyarat Instalasi

Sebelum Anda dapat membangun dan menjalankan project ini, pastikan Anda memiliki:

1.  **Android Studio** (Rekomendasi versi terbaru, misal: Iguana atau Jellyfish).
2.  **JDK 17** (Project dikonfigurasi untuk Java 17).
3.  **Akun Firebase:**
      * Project Firebase yang sudah di-setup.
      * File `google-services.json` yang valid dari project Firebase Anda, ditempatkan di direktori `app/`.
      * Layanan (Authentication, Realtime Database, Storage) harus diaktifkan.
4.  **Akun Midtrans:**
      * Kunci API (Client & Server Key) untuk mode **Sandbox** dari dashboard Midtrans.

## Susunan Project

Struktur file utama dalam project ini diatur sebagai berikut:

```
GadjahDjaya/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/app/gadjahdjaya/
│   │   │   │   ├── activity/     (Contoh: HomeActivity, KasirActivity, LoginActivity)
│   │   │   │   ├── fragment/     (Contoh: BerandaFragment, MenuFragment, StokBahanBakuFragment)
│   │   │   │   ├── adapter/      (Contoh: MenuAdapter, BahanBakuAdapter, TransaksiHariAdapter)
│   │   │   │   ├── model/        (Contoh: MenuItem.kt, BahanBaku.kt, Transaksi.kt)
│   │   │   │   └── utils/        (Contoh: CsvExporter.kt, Utils.kt)
│   │   │   ├── res/
│   │   │   │   ├── layout/       (File XML untuk layout Activity & Fragment)
│   │   │   │   ├── drawable/     (Ikon dan aset gambar)
│   │   │   │   └── navigation/   (Grafik navigasi untuk Navigation Component)
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts          (Konfigurasi build level aplikasi)
│   └── google-services.json      (File konfigurasi Firebase - *Perlu ditambahkan manual*)
├── gradle/
│   └── libs.versions.toml        (Katalog versi dependensi)
├── build.gradle.kts              (Konfigurasi build level project)
└── settings.gradle.kts           (Pengaturan project)
```

## Contoh Penggunaan (Instalasi)

Untuk menjalankan aplikasi ini secara lokal:

1.  **Clone** repositori ini:
    ```sh
    https://github.com/zoymelvin/GadjahDjaya
    ```
2.  **Buka** project di Android Studio.
3.  **Siapkan Firebase:**
      * Buka [Firebase Console](https://console.firebase.google.com/) dan buat project baru.
      * Tambahkan aplikasi Android baru dengan nama paket (applicationId) `com.app.gadjahdjaya`.
      * Unduh file `google-services.json` yang dihasilkan.
      * Tempatkan file `google-services.json` tersebut ke dalam direktori `GadjahDjaya/app/`.
      * Aktifkan **Authentication** (Email/Password) dan **Realtime Database** (atur rules ke mode tes jika perlu), serta **Storage**.
4.  **Siapkan Midtrans:**
      * Login ke [Dashboard Midtrans Sandbox](https://www.google.com/search?q=https://dashboard.sandbox.midtrans.com/).
      * Dapatkan **Client Key** dan **Server Key** Anda.
      * Masukkan kunci ini di tempat yang sesuai dalam kode (kemungkinan di dalam file constants atau `MidtransPaymentActivity.kt`).
5.  **Sync Gradle** dan **Build Project**.
6.  **Run** aplikasi pada emulator atau perangkat Android fisik (Min SDK 29).
