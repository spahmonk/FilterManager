package com.example.hohfiltermanager.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.hohfiltermanager.R
import com.example.hohfiltermanager.data.ComponentType

class SimpleAddFilterDialogFragment : DialogFragment() {

    var onFilterAdded: ((String, String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_simple_add_filter, null)

        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val locationEditText = dialogView.findViewById<EditText>(R.id.locationEditText)

        return AlertDialog.Builder(requireContext())
            .setTitle("Добавить систему фильтров")
            .setView(dialogView)
            .setPositiveButton("Создать") { dialog, which ->
                val name = nameEditText.text.toString().trim()
                val location = locationEditText.text.toString().trim()

                if (name.isNotEmpty()) {
                    onFilterAdded?.invoke(name, location)
                } else {
                    nameEditText.error = "Введите название системы"
                }
            }
            .setNegativeButton("Отмена", null)
            .create()
    }
}