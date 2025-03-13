package com.app.gadjahdjaya.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.app.gadjahdjaya.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class PengeluaranBottomSheet(
    private val onExpenseAdded: (amount: Double, description: String, paymentMethod: String) -> Unit
) : DialogFragment() {

    private var current = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pengeluaran, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val spPaymentMethod = view.findViewById<Spinner>(R.id.spPaymentMethod)
        val btnSaveExpense = view.findViewById<Button>(R.id.btnSaveExpense)

        val paymentMethods = arrayOf("Cash", "Transfer", "QRIS", "E-Wallet")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, paymentMethods)
        spPaymentMethod.adapter = adapter

        etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != current) {
                    etAmount.removeTextChangedListener(this)

                    val cleanString = s.toString().replace(".", "")
                    val parsed = cleanString.toDoubleOrNull() ?: 0.0

                    val formatter: NumberFormat = DecimalFormat("#,###", DecimalFormatSymbols(Locale("id", "ID")))
                    val formatted = formatter.format(parsed).replace(",", ".")

                    current = formatted
                    etAmount.setText(formatted)
                    etAmount.setSelection(formatted.length)

                    etAmount.addTextChangedListener(this)
                }
            }
        })

        btnSaveExpense.setOnClickListener {
            val amountString = etAmount.text.toString().replace(".", "")
            val amount = amountString.toDoubleOrNull() ?: 0.0
            val description = etDescription.text.toString().trim()
            val paymentMethod = spPaymentMethod.selectedItem.toString()

            if (amount > 0 && description.isNotEmpty()) {
                onExpenseAdded(amount, description, paymentMethod)
                dismiss()
            } else {
                etAmount.error = if (amount <= 0) "Masukkan nominal!" else null
                etDescription.error = if (description.isEmpty()) "Masukkan deskripsi!" else null
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
