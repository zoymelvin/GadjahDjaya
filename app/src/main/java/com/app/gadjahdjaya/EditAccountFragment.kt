package com.app.gadjahdjaya

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.gadjahdjaya.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditAccountFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: View
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_account, container, false)
        etName = view.findViewById(R.id.et_name)
        etEmail = view.findViewById(R.id.et_email)
        etPhone = view.findViewById(R.id.et_phone)
        btnSave = view.findViewById(R.id.btn_save)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        btnSave.setOnClickListener {
            saveUserData()
        }

        return view
    }

    private fun saveUserData() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val phone = etPhone.text.toString()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val user = User(name, email, phone)
            database.child(userId).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Data updated successfully", Toast.LENGTH_SHORT).show()
                    // Berpindah ke ActivityHome
                    val intent = Intent(activity, HomeActivity::class.java)
                    startActivity(intent)
                    activity?.finish() // Opsional, untuk menutup aktivitas saat ini jika diperlukan
                } else {
                    Toast.makeText(activity, "Failed to update data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    data class User(val name: String, val email: String, val phone: String)
}
