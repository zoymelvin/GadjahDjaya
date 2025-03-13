package com.app.gadjahdjaya


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.ImageButton
import com.app.gadjahdjaya.ui.FragmentKeuangan
import com.app.gadjahdjaya.ui.menu.MenuFragment
import com.app.gadjahdjaya.ui.stokbahan.StokBahanBakuFragment

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val fab: ImageButton = findViewById(R.id.fab)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_beranda -> {
                    openFragment(BerandaFragment())
                    true
                }
                R.id.navigation_menu -> {
                    openFragment(MenuFragment())
                    true
                }
                R.id.navigation_keuangan -> {
                    openFragment(FragmentKeuangan())
                    true
                }
                R.id.navigation_bahanbaku -> {
                    openFragment(StokBahanBakuFragment()) // ✅ Perbaikan di sini
                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {
            openFragment(KasirMenuFragment())
        }

        // Set default fragment
        if (savedInstanceState == null) {
            openFragment(BerandaFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
