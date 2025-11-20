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

        // Получаем данные из Intent
        filterId = intent.getLongExtra("filter_id", -1)
        filterName = intent.getStringExtra("filter_name") ?: "Фильтр"

        // Инициализация репозитория
        val database = AppDatabase.getInstance(this)
        repository = FilterRepository(database)

        // Инициализация views
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

        // Временно устанавливаем значения
        filterLocationText.text = "Местоположение: Загрузка..."
        installationDateText.text = "Дата установки: Загрузка..."
    }

    private fun setupRecyclerView() {
        adapter = ComponentAdapter(
            onComponentClick = { component -> openComponentDetails(component) },
            onReplaceClick = { component -> replaceComponent(component) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Временно добавляем тестовые компоненты
        loadTestComponents()
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
            }
        }
    }

    private fun updateFilterInfo(filter: Filter) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        filterLocationText.text = "Местоположение: ${filter.location}"
        installationDateText.text = "Дата установки: ${dateFormat.format(Date(filter.installationDate))}"

        updateMaintenanceInfo(filter.components)
    }

    private fun loadTestComponents() {
        // Временные тестовые данные
        val testComponents = listOf(
            ComponentType.PREDFILTER.copy(
                id = 1,
                filterId = filterId,
                lastReplacementDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 150), // 150 дней назад
                nextReplacementDate = ComponentType.PREDFILTER.calculateNextReplacement()
            ),
            ComponentType.CARBON_FILTER.copy(
                id = 2,
                filterId = filterId,
                lastReplacementDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 120), // 120 дней назад
                nextReplacementDate = ComponentType.CARBON_FILTER.calculateNextReplacement()
            ),
            ComponentType.ACCUMULATOR_TANK.copy(
                id = 3,
                filterId = filterId,
                lastReplacementDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 400), // 400 дней назад
                nextReplacementDate = ComponentType.ACCUMULATOR_TANK.calculateNextReplacement()
            )
        )

        installedComponents.clear()
        installedComponents.addAll(testComponents)
        adapter.submitList(installedComponents)
        updateMaintenanceInfo(installedComponents)
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

        // Статистика по компонентам
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
                nextReplacementDate = component.calculateNextReplacement()
            )

            val componentId = repository.addComponentToFilter(filterId, componentWithDates)

            // Обновляем локальный список
            val addedComponent = componentWithDates.copy(id = componentId)
            installedComponents.add(addedComponent)
            adapter.submitList(installedComponents.toList())
            updateMaintenanceInfo(installedComponents)

            android.widget.Toast.makeText(
                this@FilterDetailsActivity,
                "Компонент ${component.name} добавлен",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openComponentDetails(component: FilterComponent) {
        val intent = android.content.Intent(this, ComponentDetailsActivity::class.java).apply {
            putExtra("component_id", component.id)
            putExtra("component_name", component.name)
            putExtra("installation_instructions", component.installationInstructions)
            putExtra("video_url", component.videoUrl)
            putExtra("purchase_url", component.purchaseUrl)
            putExtra("last_replacement_date", component.lastReplacementDate)
            putExtra("next_replacement_date", component.nextReplacementDate ?: -1L)
            putExtra("lifespan_months", component.lifespanMonths)
        }
        startActivity(intent)
    }

    private fun replaceComponent(component: FilterComponent) {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

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

            // Обновляем в базе данных
            repository.updateComponentReplacement(component.id, replacementDate)

            // Обновляем локальный список
            val index = installedComponents.indexOfFirst { it.id == component.id }
            if (index != -1) {
                val updatedComponent = component.copy(
                    lastReplacementDate = replacementDate,
                    nextReplacementDate = component.calculateNextReplacement()
                )

                installedComponents[index] = updatedComponent
                adapter.submitList(installedComponents.toList())
                updateMaintenanceInfo(installedComponents)

                android.widget.Toast.makeText(
                    this@FilterDetailsActivity,
                    "Замена ${component.name} подтверждена",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getDateMonthsFromNow(months: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, months)
        return calendar.timeInMillis
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возвращении на экран
        loadFilterData()
    }
}