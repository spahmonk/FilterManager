package com.example.hohfiltermanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hohfiltermanager.data.ComponentType
import com.example.hohfiltermanager.data.Filter
import com.example.hohfiltermanager.data.FilterComponent
import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.presentation.AddComponentDialogFragment
import com.example.hohfiltermanager.presentation.ComponentAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class FilterDetailsActivity : AppCompatActivity() {

    companion object {
        private const val COMPONENT_DETAILS_REQUEST = 1001
    }

    private lateinit var database: AppDatabase
    private lateinit var adapter: ComponentAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterNameText: TextView
    private lateinit var filterLocationText: TextView
    private lateinit var installationDateText: TextView
    private lateinit var addComponentButton: Button

    private var filterId: Long = -1
    private var filterName: String = ""
    private val installedComponents = mutableListOf<FilterComponent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_details)

        // Получаем данные из Intent
        filterId = intent.getLongExtra("filter_id", -1)
        filterName = intent.getStringExtra("filter_name") ?: "Фильтр"

        // Инициализация views
        recyclerView = findViewById(R.id.componentsRecyclerView)
        filterNameText = findViewById(R.id.filterNameText)
        filterLocationText = findViewById(R.id.filterLocationText)
        installationDateText = findViewById(R.id.installationDateText)
        addComponentButton = findViewById(R.id.addComponentButton)

        database = AppDatabase.getInstance(this)
        setupUI()
        setupRecyclerView()
        setupClickListeners()
        loadFilterDetails()
    }

    private fun setupUI() {
        filterNameText.text = filterName
        title = "Журнал: $filterName"

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    }

    private fun setupRecyclerView() {
        adapter = ComponentAdapter(
            onComponentClick = { component -> openComponentDetails(component) },
            onReplaceClick = { component -> replaceComponent(component) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        addComponentButton.setOnClickListener {
            showAddComponentDialog()
        }

        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }
    }

    private fun loadFilterDetails() {
        // Временно добавляем тестовые компоненты
        val testComponents = listOf(
            ComponentType.PREDFILTER.copy(
                lastReplacementDate = Date(),
                nextReplacementDate = getDateMonthsFromNow(6),
                isInstalled = true
            ),
            ComponentType.CARBON_FILTER.copy(
                lastReplacementDate = Date(),
                nextReplacementDate = getDateMonthsFromNow(6),
                isInstalled = true
            )
        )

        installedComponents.clear()
        installedComponents.addAll(testComponents)
        adapter.submitList(installedComponents)

        updateMaintenanceInfo()
    }

    private fun updateMaintenanceInfo() {
        val nextMaintenance = installedComponents
            .filter { it.nextReplacementDate != null }
            .minByOrNull { it.nextReplacementDate!! }

        val maintenanceText = findViewById<TextView>(R.id.nextMaintenanceText)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        if (nextMaintenance != null) {
            maintenanceText.text = "Следующая замена: ${dateFormat.format(nextMaintenance.nextReplacementDate)}"
            maintenanceText.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            maintenanceText.text = "Все компоненты в порядке"
            maintenanceText.setTextColor(getColor(android.R.color.holo_green_dark))
        }
    }

    private fun showAddComponentDialog() {
        val dialog = AddComponentDialogFragment()
        dialog.onComponentAdded = { component ->
            addComponentToFilter(component)
        }
        dialog.show(supportFragmentManager, "AddComponentDialog")
    }

    private fun addComponentToFilter(component: FilterComponent) {
        val componentWithDates = component.copy(
            lastReplacementDate = Date(),
            nextReplacementDate = getDateMonthsFromNow(component.lifespanMonths),
            isInstalled = true
        )

        installedComponents.add(componentWithDates)
        adapter.submitList(installedComponents.toList())
        updateMaintenanceInfo()

        android.widget.Toast.makeText(this, "Компонент ${component.name} добавлен", android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun openComponentDetails(component: FilterComponent) {
        val intent = android.content.Intent(this, ComponentDetailsActivity::class.java).apply {
            putExtra("component_id", component.id)
            putExtra("component_name", component.name)
            putExtra("installation_instructions", component.installationInstructions)
            putExtra("video_url", component.videoUrl)
            putExtra("purchase_url", component.purchaseUrl)
            putExtra("last_replacement_date", component.lastReplacementDate?.time ?: -1)
            putExtra("next_replacement_date", component.nextReplacementDate?.time ?: -1)
        }
        startActivityForResult(intent, COMPONENT_DETAILS_REQUEST)
    }

    private fun replaceComponent(component: FilterComponent) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        android.app.AlertDialog.Builder(this)
            .setTitle("Подтверждение замены")
            .setMessage("Вы подтверждаете замену компонента \"${component.name}\"?")
            .setPositiveButton("Подтвердить") { _, _ ->
                performComponentReplacement(component)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun performComponentReplacement(component: FilterComponent) {
        val index = installedComponents.indexOfFirst { it.id == component.id }
        if (index != -1) {
            val updatedComponent = component.copy(
                lastReplacementDate = Date(),
                nextReplacementDate = getDateMonthsFromNow(component.lifespanMonths)
            )

            installedComponents[index] = updatedComponent
            adapter.submitList(installedComponents.toList())
            updateMaintenanceInfo()

            android.widget.Toast.makeText(this, "Замена ${component.name} подтверждена", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDateMonthsFromNow(months: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, months)
        return calendar.time
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == COMPONENT_DETAILS_REQUEST && resultCode == RESULT_OK) {
            val replacedComponentId = data?.getLongExtra("replaced_component_id", -1) ?: -1
            val replacementDateMillis = data?.getLongExtra("replacement_date", -1) ?: -1

            if (replacedComponentId != -1L && replacementDateMillis != -1L) {
                updateComponentReplacement(replacedComponentId, Date(replacementDateMillis))
            }
        }
    }

    private fun updateComponentReplacement(componentId: Long, replacementDate: Date) {
        val index = installedComponents.indexOfFirst { it.id == componentId }
        if (index != -1) {
            val component = installedComponents[index]
            val updatedComponent = component.copy(
                lastReplacementDate = replacementDate,
                nextReplacementDate = getDateMonthsFromNow(component.lifespanMonths)
            )

            installedComponents[index] = updatedComponent
            adapter.submitList(installedComponents.toList())
            updateMaintenanceInfo()

            android.widget.Toast.makeText(this, "Замена компонента обновлена", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}