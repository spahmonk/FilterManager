package com.example.hohfiltermanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hohfiltermanager.data.Filter
import com.example.hohfiltermanager.data.FilterRepository
import com.example.hohfiltermanager.data.ComponentType
import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.data.local.FilterEntity
import com.example.hohfiltermanager.databinding.ActivityMainBinding
import com.example.hohfiltermanager.presentation.SimpleAddFilterDialogFragment
import com.example.hohfiltermanager.presentation.FilterAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: FilterRepository
    private lateinit var adapter: FilterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getInstance(this)
        repository = FilterRepository(database)

        setupRecyclerView()
        setupClickListeners()
        loadFilters()

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
            showSimpleAddFilterDialog()
        }

        binding.testDataButton.setOnClickListener {
            addSampleFilter()
        }
    }

    private fun loadFilters() {
        lifecycleScope.launch {
            repository.getAllFilters().collect { filters ->
                adapter.submitList(filters)
                binding.emptyState.visibility = if (filters.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                updateStats(filters)
            }
        }
    }

    private fun updateStats(filters: List<Filter>) {
        val totalFilters = filters.size
        val totalComponents = filters.sumOf { it.components.size }
        val needsAttention = filters.count { it.getComponentsNeedingReplacement().isNotEmpty() }

        binding.statsText.text =
            "Фильтры: $totalFilters | Требуют внимания: $needsAttention"
    }

    private fun showSimpleAddFilterDialog() {
        val dialog = SimpleAddFilterDialogFragment()
        dialog.onFilterAdded = { name, location ->
            lifecycleScope.launch {
                val filterEntity = FilterEntity(
                    id = System.currentTimeMillis(),
                    name = name,
                    location = location,
                    installationDate = System.currentTimeMillis()
                )

                val defaultComponents = listOf(
                    ComponentType.PREDFILTER.copy(
                        lastReplacementDate = System.currentTimeMillis()
                    ),
                    ComponentType.CARBON_FILTER.copy(
                        lastReplacementDate = System.currentTimeMillis()
                    )
                )

                repository.addFilter(filterEntity, defaultComponents)

                android.widget.Toast.makeText(
                    this@MainActivity,
                    "Фильтр '$name' добавлен!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        dialog.show(supportFragmentManager, "SimpleAddFilterDialog")
    }

    private fun openFilterDetails(filter: Filter) {
        val intent = android.content.Intent(this, FilterDetailsActivity::class.java).apply {
            putExtra("filter_id", filter.id)
            putExtra("filter_name", filter.name)
        }
        startActivity(intent)
    }

    private fun deleteFilter(filter: Filter) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Удаление фильтра")
            .setMessage("Удалить фильтр \"${filter.name}\"?")
            .setPositiveButton("Удалить") { dialog, which ->
                lifecycleScope.launch {
                    repository.deleteFilter(filter)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun checkAndAddSampleData() {
        lifecycleScope.launch {
            repository.getAllFilters().collect { filters ->
                if (filters.isEmpty()) {
                    showSampleDataPrompt()
                }
                return@collect
            }
        }
    }

    private fun showSampleDataPrompt() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Добро пожаловать!")
            .setMessage("Добавить пример фильтра?")
            .setPositiveButton("Да") { dialog, which ->
                addSampleFilter()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun addSampleFilter() {
        lifecycleScope.launch {
            val sampleFilter = FilterEntity(
                id = System.currentTimeMillis(),
                name = "Кухонная система очистки",
                location = "Под раковиной на кухне",
                installationDate = System.currentTimeMillis()
            )

            val components = listOf(
                ComponentType.PREDFILTER.copy(
                    lastReplacementDate = System.currentTimeMillis()
                ),
                ComponentType.CARBON_FILTER.copy(
                    lastReplacementDate = System.currentTimeMillis()
                ),
                ComponentType.MEMBRANE.copy(
                    lastReplacementDate = System.currentTimeMillis()
                ),
                ComponentType.ACCUMULATOR_TANK.copy(
                    lastReplacementDate = System.currentTimeMillis()
                )
            )

            repository.addFilter(sampleFilter, components)

            android.widget.Toast.makeText(
                this@MainActivity,
                "Пример системы добавлен!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFilters()
    }
}