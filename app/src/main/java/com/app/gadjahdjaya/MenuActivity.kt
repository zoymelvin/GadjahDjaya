package com.app.gadjahdjaya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.gadjahdjaya.ui.menu.MenuFragment

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MenuFragment())
                .commit()
        }
    }
}
