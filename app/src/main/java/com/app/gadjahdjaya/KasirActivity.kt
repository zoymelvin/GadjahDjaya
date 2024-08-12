package com.app.gadjahdjaya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class KasirActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kasir)

        // Inisialisasi fragmen
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_kasir, KasirMenuFragment())
                .commit()
        }
    }
}
