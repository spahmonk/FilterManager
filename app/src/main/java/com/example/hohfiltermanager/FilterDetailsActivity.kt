package com.example.hohfiltermanager

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hohfiltermanager.data.ComponentType
import com.example.hohfiltermanager.data.Filter
import com.example.hohfiltermanager.data.FilterComponent
import com.example.hohfiltermanager.data.FilterRepository
import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.presentation.ComponentAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FilterDetailsActivity : AppCompatActivity() {

    private lateinit var repository: FilterRepository
    private lateinit var adapter: ComponentAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterNameText: TextView
    private lateinit var filterLocationText: TextView
    private lateinit var installationDateText: TextView
    private lateinit var nextMaintenanceText: TextView
    private lateinit var addComponentButton: Button

    private var filterId: Long = -1
    private var filterName: String = ""
    private val installedComponents = mutableListOf<FilterComponent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter_details)

        filterId = intent.getLongExtra("filter_id", -1)
        filterName = intent.getStringExtra("filter_name") ?: "Фильтр"

        val database = AppDatabase.getInstance(this)
        repository = FilterRepository(database)

        initializeViews()
        setupUI()
        setupRecyclerView()
        setupClickListeners()
        loadFilterData()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.componentsRecyclerView)
        filterNameText = findViewById(R.id.filterNameText)
        filterLocationText = findViewById(R.id.filterLocationText)
        installationDateText = findViewById(R.id.installationDateText)
        nextMaintenanceText = findViewById(R.id.nextMaintenanceText)
        addComponentButton = findViewById(R.id.addComponentButton)
    }

    private fun setupUI() {
        filterNameText.text = filterName
        title = "Журнал: $filterName"
    }

    private fun setupRecyclerView() {
        adapter = ComponentAdapter(
            onComponentClick = { component -> openComponentDetails(component) },
            onReplaceClick = { component -> replaceComponent(component) },
            onDeleteClick = { component -> deleteComponent(component) }
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

    private fun loadFilterData() {
        lifecycleScope.launch {
            val filter = repository.getFilterWithComponents(filterId)
            filter?.let {
                updateFilterInfo(it)
                installedComponents.clear()
                installedComponents.addAll(it.components)
                adapter.submitList(installedComponents.toList())
                updateMaintenanceInfo(installedComponents)
            }
        }
    }

    private fun updateFilterInfo(filter: Filter) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        filterLocationText.text = "Местоположение: ${filter.location}"
        installationDateText.text = "Дата установки: ${dateFormat.format(Date(filter.installationDate))}"
        updateMaintenanceInfo(filter.components)
    }

    private fun updateMaintenanceInfo(components: List<FilterComponent>) {
        val nextMaintenance = components
            .filter { it.nextReplacementDate != null }
            .minByOrNull { it.nextReplacementDate!! }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        if (nextMaintenance != null) {
            val nextDateString = dateFormat.format(Date(nextMaintenance.nextReplacementDate!!))
            nextMaintenanceText.text = "Следующая замена: $nextDateString"

            if (nextMaintenance.needsReplacement()) {
                nextMaintenanceText.setTextColor(getColor(android.R.color.holo_red_dark))
                nextMaintenanceText.text = "🚨 ТРЕБУЕТ ЗАМЕНЫ: ${nextMaintenance.name}"
            } else if (nextMaintenance.isReplacementSoon(30)) {
                nextMaintenanceText.setTextColor(getColor(android.R.color.holo_orange_dark))
                val daysLeft = nextMaintenance.getDaysUntilReplacement()
                nextMaintenanceText.text = "⚠️ Скоро замена: ${nextMaintenance.name} (осталось $daysLeft дней)"
            } else {
                nextMaintenanceText.setTextColor(getColor(android.R.color.holo_green_dark))
            }
        } else {
            nextMaintenanceText.text = "Все компоненты в порядке"
            nextMaintenanceText.setTextColor(getColor(android.R.color.holo_green_dark))
        }

        val totalComponents = components.size
        val needsReplacement = components.count { it.needsReplacement() }
        val replacementSoon = components.count { it.isReplacementSoon(30) && !it.needsReplacement() }

        val statsText = findViewById<TextView>(R.id.statsText)
        statsText.text = "Компоненты: $totalComponents | Требуют замены: $needsReplacement | Скоро замена: $replacementSoon"
    }

    private fun showAddComponentDialog() {
        val componentNames = ComponentType.ALL_COMPONENTS.map { it.name }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle("Добавить компонент")
            .setItems(componentNames) { _, which ->
                val selectedComponent = ComponentType.ALL_COMPONENTS[which]
                addComponentToFilter(selectedComponent)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun addComponentToFilter(component: FilterComponent) {
        lifecycleScope.launch {
            val componentWithDates = component.copy(
                filterId = filterId,
                lastReplacementDate = System.currentTimeMillis(),
                nextReplacementDate = component.calculateNextReplacement(System.currentTimeMillis())
            )

            val componentId = repository.addComponentToFilter(filterId, componentWithDates)
            loadFilterData() // Перезагружаем данные

            android.widget.Toast.makeText(
                this@FilterDetailsActivity,
                "Компонент ${component.name} добавлен",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteComponent(component: FilterComponent) {
        lifecycleScope.launch {
            repository.deleteComponent(component.id)
            loadFilterData() // Перезагружаем данные

            android.widget.Toast.makeText(
                this@FilterDetailsActivity,
                "Компонент ${component.name} удален",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openComponentDetails(component: FilterComponent) {
        val intent = android.content.Intent(this, ComponentDetailsActivity::class.java).apply {
            putExtra("component_id", component.id)
            putExtra("component_name", component.name)
            putExtra("lifespan_months", component.lifespanMonths)
            putExtra("last_replacement_date", component.lastReplacementDate)
            putExtra("next_replacement_date", component.nextReplacementDate ?: -1L)
        }
        startActivity(intent)
    }

    private fun replaceComponent(component: FilterComponent) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Подтверждение замены")
            .setMessage("Вы подтверждаете замену компонента \"${component.name}\"?")
            .setPositiveButton("Подтвердить замену") { _, _ ->
                performComponentReplacement(component)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun performComponentReplacement(component: FilterComponent) {
        lifecycleScope.launch {
            val replacementDate = System.currentTimeMillis()
            repository.updateComponentReplacement(component.id, replacementDate)
            loadFilterData() // Перезагружаем данные

            android.widget.Toast.makeText(
                this@FilterDetailsActivity,
                "Замена ${component.name} подтверждена",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFilterData()
    }
}