package com.app.gadjahdjaya.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.gadjahdjaya.EditAccountFragment
import com.app.gadjahdjaya.QrisSettingActivity
import com.app.gadjahdjaya.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle click on edit account
        view.findViewById<View>(R.id.edit_account).setOnClickListener {
            navigateToEditAccount()
        }

        // Handle click on qris
        view.findViewById<View>(R.id.qris).setOnClickListener {
            navigateToQris()
        }
    }

    private fun navigateToEditAccount() {
        val fragment = EditAccountFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)  // Optional: Add to back stack if needed
            .commit()
    }

    private fun navigateToQris() {
        val intent = Intent(context, QrisSettingActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_QRIS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_QRIS && resultCode == Activity.RESULT_OK) {
            // Tangani hasil jika diperlukan
        }
    }

    companion object {
        private const val REQUEST_CODE_QRIS = 1
    }
}
