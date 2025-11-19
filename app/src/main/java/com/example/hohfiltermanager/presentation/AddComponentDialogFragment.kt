package com.example.hohfiltermanager.presentation

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.hohfiltermanager.data.ComponentType
import com.example.hohfiltermanager.data.FilterComponent

class AddComponentDialogFragment : DialogFragment() {

    var onComponentAdded: ((FilterComponent) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val componentNames = ComponentType.ALL_COMPONENTS.map { it.name }.toTypedArray()

        return AlertDialog.Builder(requireContext())
            .setTitle("Добавить компонент")
            .setItems(componentNames) { dialog, which ->
                val selectedComponent = ComponentType.ALL_COMPONENTS[which]
                onComponentAdded?.invoke(selectedComponent)
            }
            .setNegativeButton("Отмена", null)
            .create()
    }
}