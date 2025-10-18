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

## Kontribusi

Kontribusi sangat kami hargai\! Jika Anda ingin berkontribusi pada project ini, silakan ikuti langkah-langkah berikut:

1.  **Fork** repositori ini.
2.  Buat *branch* fitur baru (`git checkout -b fitur/NamaFiturBaru`).
3.  *Commit* perubahan Anda (`git commit -m 'Menambahkan fitur A'`).
4.  *Push* ke *branch* Anda (`git push origin fitur/NamaFiturBaru`).
5.  Buka **Pull Request**.

## Lisensi

Project ini dilisensikan di bawah **Lisensi MIT**. Lihat file `LICENSE` untuk detail lebih lanjut.

```markdown
MIT License

Copyright (c) [Tahun] [Nama Pemilik Hak Cipta]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
