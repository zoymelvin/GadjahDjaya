package com.app.gadjahdjaya

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Referensi ke elemen UI
        val btnLogin = findViewById<Button>(R.id.R_btn_1)
        val emailEditText = findViewById<EditText>(R.id.edt_email)
        val passwordEditText = findViewById<EditText>(R.id.edt_password)
        val tvRegister = findViewById<TextView>(R.id.tv_register) // Referensi ke TextView Register

        // Menetapkan listener untuk tombol login
        btnLogin.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Validasi sederhana
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Login menggunakan Firebase Auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Login berhasil, navigasi ke HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        // Pindahkan finish() setelah memulai aktivitas baru
                        finish()
                        Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    } else {
                        // Login gagal, tampilkan pesan kesalahan
                        Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Menetapkan listener untuk TextView Register
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
