package com.example.hohfiltermanager.presentation

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.hohfiltermanager.data.ComponentType
import java.text.SimpleDateFormat
import com.example.hohfiltermanager.R
import java.util.*

class SimpleAddFilterDialogFragment : DialogFragment() {

    var onFilterAdded: ((String, String, Long, List<com.example.hohfiltermanager.data.FilterComponent>) -> Unit)? = null

    private val selectedComponents = mutableListOf<com.example.hohfiltermanager.data.FilterComponent>()
    private var installationDate = System.currentTimeMillis()

    private lateinit var nameEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var installationDateButton: Button
    private lateinit var componentsSpinner: Spinner
    private lateinit var addComponentButton: Button
    private lateinit var selectedComponentsText: TextView
    private lateinit var accumulatorCheckbox: CheckBox
    private lateinit var systemTypeSpinner: Spinner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_simple_add_filter, null)

        initializeViews(dialogView)
        setupSystemTypeSpinner()
        setupComponentsSpinner()
        setupDatePicker()
        setupClickListeners()

        return AlertDialog.Builder(requireContext())
            .setTitle("Добавить систему фильтров")
            .setView(dialogView)
            .setPositiveButton("Создать систему") { dialog, which ->
                saveFilter()
            }
            .setNegativeButton("Отмена", null)
            .create()
    }

    private fun initializeViews(dialogView: android.view.View) {
        nameEditText = dialogView.findViewById(R.id.nameEditText)
        locationEditText = dialogView.findViewById(R.id.locationEditText)
        installationDateButton = dialogView.findViewById(R.id.installationDateButton)
        componentsSpinner = dialogView.findViewById(R.id.componentsSpinner)
        addComponentButton = dialogView.findViewById(R.id.addComponentButton)
        selectedComponentsText = dialogView.findViewById(R.id.selectedComponentsText)
        accumulatorCheckbox = dialogView.findViewById(R.id.accumulatorCheckbox)
        systemTypeSpinner = dialogView.findViewById(R.id.systemTypeSpinner)

        updateDateButtonText()
        updateSelectedComponentsList()
    }

    private fun setupSystemTypeSpinner() {
        val systemTypes = arrayOf(
            "Простая система (2 компонента)",
            "Система обратного осмоса (4 компонента)",
            "Полная система (все компоненты)",
            "Кастомная система"
        )

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, systemTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        systemTypeSpinner.adapter = adapter

        systemTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                when (position) {
                    0 -> setupBasicSystem() // Простая система
                    1 -> setupOsmosisSystem() // Система обратного осмоса
                    2 -> setupFullSystem() // Полная система
                    // 3 -> Кастомная - оставляем как есть
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupBasicSystem() {
        selectedComponents.clear()
        val basicComponents = ComponentType.getComponentsForSystem("BASIC")
        selectedComponents.addAll(basicComponents.map { it.copy(lastReplacementDate = installationDate) })
        updateSelectedComponentsList()
        accumulatorCheckbox.isChecked = false
    }

    private fun setupOsmosisSystem() {
        selectedComponents.clear()
        val osmosisComponents = ComponentType.getComponentsForSystem("OSMOSIS")
        selectedComponents.addAll(osmosisComponents.map { it.copy(lastReplacementDate = installationDate) })
        updateSelectedComponentsList()
        accumulatorCheckbox.isChecked = true
    }

    private fun setupFullSystem() {
        selectedComponents.clear()
        val allComponents = ComponentType.ALL_COMPONENTS
        selectedComponents.addAll(allComponents.map { it.copy(lastReplacementDate = installationDate) })
        updateSelectedComponentsList()
        accumulatorCheckbox.isChecked = true
    }

    private fun setupComponentsSpinner() {
        val componentNames = ComponentType.ALL_COMPONENTS.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, componentNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        componentsSpinner.adapter = adapter
    }

    private fun setupDatePicker() {
        installationDateButton.setOnClickListener {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = installationDate
            }

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selectedCalendar = Calendar.getInstance().apply {
                        set(year, month, day, 0, 0, 0)
                    }
                    installationDate = selectedCalendar.timeInMillis
                    updateDateButtonText()

                    // Обновляем даты установки для всех выбранных компонентов
                    selectedComponents.forEachIndexed { index, component ->
                        selectedComponents[index] = component.copy(lastReplacementDate = installationDate)
                    }
                    updateSelectedComponentsList()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateDateButtonText() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        installationDateButton.text = "📅 Дата установки: ${dateFormat.format(Date(installationDate))}"
    }

    private fun setupClickListeners() {
        addComponentButton.setOnClickListener {
            val selectedPosition = componentsSpinner.selectedItemPosition
            if (selectedPosition >= 0) {
                val selectedComponent = ComponentType.ALL_COMPONENTS[selectedPosition]
                if (selectedComponents.none { it.componentTypeId == selectedComponent.componentTypeId }) {
                    val componentToAdd = selectedComponent.copy(
                        filterId = 0,
                        lastReplacementDate = installationDate,
                        nextReplacementDate = selectedComponent.calculateNextReplacement()
                    )
                    selectedComponents.add(componentToAdd)
                    updateSelectedComponentsList()

                    // Автоматически отмечаем бак-накопитель если добавлена мембрана
                    if (selectedComponent.componentTypeId == ComponentType.MEMBRANE.componentTypeId) {
                        accumulatorCheckbox.isChecked = true
                    }
                } else {
                    showToast("Этот компонент уже добавлен")
                }
            }
        }

        accumulatorCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val accumulatorType = ComponentType.ACCUMULATOR_TANK
            if (isChecked) {
                if (selectedComponents.none { it.componentTypeId == accumulatorType.componentTypeId }) {
                    val accumulator = accumulatorType.copy(
                        filterId = 0,
                        lastReplacementDate = installationDate,
                        nextReplacementDate = accumulatorType.calculateNextReplacement()
                    )
                    selectedComponents.add(accumulator)
                    updateSelectedComponentsList()
                }
            } else {
                selectedComponents.removeAll { it.componentTypeId == accumulatorType.componentTypeId }
                updateSelectedComponentsList()
            }
        }
    }

    private fun updateSelectedComponentsList() {
        val componentsText = if (selectedComponents.isNotEmpty()) {
            "Выбранные компоненты (${selectedComponents.size}):\n" +
                    selectedComponents.joinToString("\n") { component ->
                        val componentType = ComponentType.getById(component.componentTypeId)
                        "• ${componentType?.name ?: component.componentTypeId} " +
                                "(${componentType?.lifespanMonths ?: 0} мес.)"
                    }
        } else {
            "❌ Компоненты не выбраны\nДобавьте картриджи и мембраны"
        }
        selectedComponentsText.text = componentsText
    }

    private fun saveFilter() {
        val name = nameEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()

        if (name.isEmpty()) {
            nameEditText.error = "Введите название системы"
            return
        }

        if (selectedComponents.isEmpty()) {
            showToast("Добавьте хотя бы один компонент")
            return
        }

        // Проверяем обязательные компоненты для системы обратного осмоса
        val hasMembrane = selectedComponents.any { it.componentTypeId == ComponentType.MEMBRANE.componentTypeId }
        val hasPredFilter = selectedComponents.any { it.componentTypeId == ComponentType.PREDFILTER.componentTypeId }

        if (hasMembrane && !hasPredFilter) {
            showToast("Для системы с мембраной необходим предфильтр")
            return
        }

        // Передаем данные обратно в MainActivity
        onFilterAdded?.invoke(name, location, installationDate, selectedComponents)
        showToast("Система фильтров создана!")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}