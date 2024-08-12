package com.app.gadjahdjaya

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Users")

        val btnRegister = findViewById<Button>(R.id.R_btn_1)
        btnRegister.setOnClickListener {
            val emailEditText = findViewById<EditText>(R.id.edt_email)
            val passwordEditText = findViewById<EditText>(R.id.edt_password)
            val nameEditText = findViewById<EditText>(R.id.edt_nama)
            val phoneEditText = findViewById<EditText>(R.id.edt_telp)

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val name = nameEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            // Validasi sederhana
            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Harap isi semua bidang", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi format email
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mendaftarkan pengguna ke Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registrasi berhasil, simpan informasi pengguna ke Realtime Database Firebase
                        val user = auth.currentUser
                        val userId = user?.uid ?: ""
                        val userMap = HashMap<String, Any>()
                        userMap["name"] = name
                        userMap["email"] = email
                        userMap["phone"] = phone

                        database.child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Informasi pengguna disimpan ke database", Toast.LENGTH_SHORT).show()
                                // Navigasi ke HomeActivity setelah registrasi berhasil
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal menyimpan informasi pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Registrasi gagal, tampilkan pesan kesalahan
                        Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Pengaturan teks untuk tautan masuk
        val loginLink = findViewById<TextView>(R.id.R_txt_lgn)
        loginLink.setOnClickListener {
            // Navigasi ke aktivitas login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}