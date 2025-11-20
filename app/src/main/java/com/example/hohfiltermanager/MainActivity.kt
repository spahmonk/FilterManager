package com.example.hohfiltermanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hohfiltermanager.data.Filter
import com.example.hohfiltermanager.data.FilterRepository
import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.data.local.FilterEntity
import com.example.hohfiltermanager.databinding.ActivityMainBinding
import com.example.hohfiltermanager.presentation.AddFilterDialogFragment
import com.example.hohfiltermanager.presentation.FilterAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: FilterRepository
    private lateinit var adapter: FilterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация репозитория
        val database = AppDatabase.getInstance(this)
        repository = FilterRepository(database)

        setupRecyclerView()
        setupClickListeners()
        loadFilters()

        // Добавляем тестовые данные если список пуст
        checkAndAddSampleData()
    }

    private fun setupRecyclerView() {
        adapter = FilterAdapter(
            onFilterClick = { filter -> openFilterDetails(filter) },
            onDeleteClick = { filter -> deleteFilter(filter) }
        )
        binding.filtersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.filtersRecyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.addFilterButton.setOnClickListener {
            showAddFilterDialog()
        }

        // Кнопка для быстрого добавления тестового фильтра
        binding.testDataButton.setOnClickListener {
            addSampleFilter()
        }
    }

    private fun loadFilters() {
        lifecycleScope.launch {
            repository.getAllFiltersWithComponents().collect { filters ->
                adapter.submitList(filters)

                // Показываем заглушку если список пуст
                binding.emptyState.visibility = if (filters.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

                // Обновляем статистику
                updateStats(filters)
            }
        }
    }

    private fun updateStats(filters: List<Filter>) {
        val totalFilters = filters.size
        val totalComponents = filters.sumOf { it.components.size }
        val componentsNeedReplacement = filters.flatMap { it.components }
            .count { it.needsReplacement() }

        binding.statsText.text =
            "Фильтры: $totalFilters | Компоненты: $totalComponents | Требуют замены: $componentsNeedReplacement"
    }

    private fun showAddFilterDialog() {
        val dialog = AddFilterDialogFragment()
        dialog.onFilterAdded = { filterEntity, components ->
            lifecycleScope.launch {
                repository.addFilter(filterEntity, components)
                // loadFilters() автоматически обновится через Flow
            }
        }
        dialog.show(supportFragmentManager, "AddFilterDialog")
    }

    private fun openFilterDetails(filter: Filter) {
        val intent = android.content.Intent(this, FilterDetailsActivity::class.java).apply {
            putExtra("filter_id", filter.id)
            putExtra("filter_name", filter.name)
        }
        startActivity(intent)
    }

    private fun deleteFilter(filter: Filter) {
        // Показываем диалог подтверждения
        android.app.AlertDialog.Builder(this)
            .setTitle("Удаление фильтра")
            .setMessage("Вы уверены, что хотите удалить фильтр \"${filter.name}\"? Все связанные компоненты также будут удалены.")
            .setPositiveButton("Удалить") { dialog, which ->
                lifecycleScope.launch {
                    repository.deleteFilter(filter)
                    // loadFilters() автоматически обновится через Flow
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun checkAndAddSampleData() {
        lifecycleScope.launch {
            val filters = repository.getAllFiltersWithComponents()
            // Проверяем первый элемент Flow чтобы понять есть ли данные
            filters.collect { filterList ->
                if (filterList.isEmpty()) {
                    // Предлагаем добавить тестовые данные
                    showSampleDataPrompt()
                }
                // Отписываемся после первой проверки
                return@collect
            }
        }
    }

    private fun showSampleDataPrompt() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Добро пожаловать!")
            .setMessage("Хотите добавить пример фильтра для ознакомления с функционалом?")
            .setPositiveButton("Добавить пример") { dialog, which ->
                addSampleFilter()
            }
            .setNegativeButton("Создать свой", null)
            .show()
    }

    private fun addSampleFilter() {
        lifecycleScope.launch {
            // Создаем тестовый фильтр
            val sampleFilter = FilterEntity(
                id = System.currentTimeMillis(),
                name = "Кухонная система",
                location = "Под раковиной на кухне",
                installationDate = System.currentTimeMillis()
            )

            // Добавляем стандартные компоненты для системы обратного осмоса
            val components = listOf(
                com.example.hohfiltermanager.data.ComponentType.PREDFILTER.copy(
                    lastReplacementDate = System.currentTimeMillis()
                ),
                com.example.hohfiltermanager.data.ComponentType.CARBON_FILTER.copy(
                    lastReplacementDate = System.currentTimeMillis()
                ),
                com.example.hohfiltermanager.data.ComponentType.MEMBRANE.copy(
                    lastReplacementDate = System.currentTimeMillis()
                ),
                com.example.hohfiltermanager.data.ComponentType.POSTFILTER.copy(
                    lastReplacementDate = System.currentTimeMillis()
                ),
                com.example.hohfiltermanager.data.ComponentType.ACCUMULATOR_TANK.copy(
                    lastReplacementDate = System.currentTimeMillis()
                )
            )

            repository.addFilter(sampleFilter, components)

            android.widget.Toast.makeText(
                this@MainActivity,
                "Пример фильтра добавлен!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Обработка кнопки назад
    private var backPressedTime = 0L
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            android.widget.Toast.makeText(
                this,
                "Нажмите еще раз для выхода",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возвращении на экран
        loadFilters()
    }
}