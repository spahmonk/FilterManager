package com.example.hohfiltermanager.presentation

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.hohfiltermanager.data.ComponentType
import com.example.hohfiltermanager.data.local.FilterEntity
import com.example.hohfiltermanager.databinding.DialogAddFilterBinding
import com.example.hohfiltermanager.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddFilterDialogFragment : DialogFragment() {

    private lateinit var binding: DialogAddFilterBinding
    private lateinit var database: AppDatabase
    var onFilterAdded: (() -> Unit)? = null

    private val selectedComponents = mutableListOf<com.example.hohfiltermanager.data.FilterComponent>()
    private var installationDate = Calendar.getInstance().time

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddFilterBinding.inflate(layoutInflater)
        database = AppDatabase.getInstance(requireContext())

        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.setTitle("Добавить фильтр")

        setupComponentsSpinner()
        setupDatePicker()
        setupClickListeners()

        return dialog
    }

    private fun setupComponentsSpinner() {
        val componentNames = ComponentType.ALL_COMPONENTS.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, componentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.componentsSpinner.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.installationDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance().apply {
                        set(year, month, day)
                    }
                    installationDate = selectedDate.time
                    binding.installationDateButton.text =
                        "Дата установки: ${android.text.format.DateFormat.format("dd.MM.yyyy", installationDate)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupClickListeners() {
        binding.addComponentButton.setOnClickListener {
            val selectedPosition = binding.componentsSpinner.selectedItemPosition
            if (selectedPosition >= 0) {
                val selectedComponent = ComponentType.ALL_COMPONENTS[selectedPosition]
                selectedComponents.add(selectedComponent)
                updateSelectedComponentsList()
            }
        }

        binding.saveButton.setOnClickListener {
            saveFilter()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun updateSelectedComponentsList() {
        val componentsText = selectedComponents.joinToString("\n") { "• ${it.name} (${it.lifespanMonths} мес.)" }
        binding.selectedComponentsText.text = componentsText
    }

    private fun saveFilter() {
        val name = binding.nameEditText.text.toString().trim()
        val location = binding.locationEditText.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameEditText.error = "Введите название"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val newFilter = FilterEntity(
                id = System.currentTimeMillis(),
                name = name,
                location = location,
                installationDate = installationDate.time
            )

            database.filterDao().insertFilter(newFilter)

            CoroutineScope(Dispatchers.Main).launch {
                onFilterAdded?.invoke()
                dismiss()
            }
        }
    }
}