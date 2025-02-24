package com.app.gadjahdjaya.ui.stokbahan

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.gadjahdjaya.adapter.BumbuDetailAdapter
import com.app.gadjahdjaya.databinding.DialogDetailBumbuBinding
import com.app.gadjahdjaya.model.BahanBaku
import com.google.firebase.database.FirebaseDatabase

class DialogDetailBumbu(
    private val kategori: String,
    private val bumbuList: List<BahanBaku>
) : DialogFragment() {

    private var _binding: DialogDetailBumbuBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance().reference.child("bahanBaku")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogDetailBumbuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (bumbuList.isEmpty()) {
            dismiss() // ✅ Jika tidak ada data, langsung tutup dialog
            return
        }

        binding.recyclerViewDetailBumbu.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDetailBumbu.adapter = BumbuDetailAdapter(
            bumbuList = bumbuList,
            onEditClick = { bahan -> showEditBahanPresentaseDialog(bahan) },
            onDeleteClick = { bahan -> showDeleteConfirmationDialog(bahan) },
            onTambahClick = { bahan -> showTambahStokDialog(bahan) }
        )

    }

    private fun showEditBahanPresentaseDialog(bahan: BahanBaku) {
        val dialog = DialogEditBahanPresentase(bahan)
        dialog.show(childFragmentManager, "DialogEditBahanPresentase")
    }

    private fun showTambahStokDialog(bahan: BahanBaku) {
        val dialog = DialogTambahStok(bahan)
        dialog.show(childFragmentManager, "DialogTambahStok")
    }

    // ✅ **Menampilkan dialog konfirmasi sebelum menghapus bahan**
    private fun showDeleteConfirmationDialog(bahan: BahanBaku) {
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus bahan '${bahan.nama}'?")
            .setPositiveButton("Ya") { _, _ -> deleteBahan(bahan) }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteBahan(bahan: BahanBaku) {
        database.child(bahan.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Bahan berhasil dihapus!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal menghapus bahan!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
