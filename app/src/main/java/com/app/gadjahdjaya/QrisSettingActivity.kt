package com.app.gadjahdjaya

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class QrisSettingActivity : AppCompatActivity() {

    private lateinit var imageViewQr: ImageView
    private lateinit var buttonUpload: Button
    private lateinit var buttonSave: Button
    private var qrImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qris_setting)

        imageViewQr = findViewById(R.id.imageView_qr)
        buttonUpload = findViewById(R.id.button_upload)
        buttonSave = findViewById(R.id.button_save)

        buttonUpload.setOnClickListener {
            openGallery()
        }

        buttonSave.setOnClickListener {
            saveQrImageToFirebase()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            qrImageUri = data?.data
            imageViewQr.setImageURI(qrImageUri)
        }
    }

    private fun saveQrImageToFirebase() {
        if (qrImageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("qris_codes/${System.currentTimeMillis()}.jpg")
            val uploadTask = storageReference.putFile(qrImageUri!!)

            uploadTask.addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    saveQrImageUriToDatabase(uri.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveQrImageUriToDatabase(uri: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("QRISCODE")
        val qrCodeId = databaseReference.push().key
        if (qrCodeId != null) {
            databaseReference.child(qrCodeId).setValue(uri).addOnSuccessListener {
                Toast.makeText(this, "QR Code saved successfully", Toast.LENGTH_SHORT).show()
                // Kembali ke SettingsFragment
                setResult(Activity.RESULT_OK)
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
