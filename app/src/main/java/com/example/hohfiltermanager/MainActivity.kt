package com.example.hohfiltermanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hohfiltermanager.data.Filter
import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.databinding.ActivityMainBinding
import com.example.hohfiltermanager.presentation.AddFilterDialogFragment
import com.example.hohfiltermanager.presentation.FilterAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: FilterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getInstance(this)
        setupRecyclerView()
        setupClickListeners()
        loadFilters()
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
    }

    private fun loadFilters() {
        lifecycleScope.launch {
            database.filterDao().getAllFilters().collect { filterEntities ->
                val filters = filterEntities.map { entity ->
                    Filter(
                        id = entity.id,
                        name = entity.name,
                        location = entity.location,
                        installationDate = entity.installationDate
                    )
                }
                adapter.submitList(filters)

                // Показываем заглушку если список пуст
                binding.emptyState.visibility = if (filters.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun showAddFilterDialog() {
        val dialog = AddFilterDialogFragment()
        dialog.onFilterAdded = {
            loadFilters() // Перезагружаем список после добавления
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
        lifecycleScope.launch {
            database.filterDao().deleteFilter(
                com.example.hohfiltermanager.data.local.FilterEntity(
                    id = filter.id,
                    name = filter.name,
                    location = filter.location,
                    installationDate = filter.installationDate
                )
            )
        }
    }
}