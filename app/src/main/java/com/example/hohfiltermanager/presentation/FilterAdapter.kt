package com.example.hohfiltermanager.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hohfiltermanager.data.Filter
import com.example.hohfiltermanager.R
import java.text.SimpleDateFormat
import java.util.*

class FilterAdapter(
    private val onFilterClick: (Filter) -> Unit,
    private val onDeleteClick: (Filter) -> Unit
) : ListAdapter<Filter, FilterAdapter.FilterViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        return FilterViewHolder(view, onFilterClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = getItem(position)
        holder.bind(filter)
    }

    class FilterViewHolder(
        itemView: View,
        private val onFilterClick: (Filter) -> Unit,
        private val onDeleteClick: (Filter) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(R.id.filterName)
        private val locationTextView: TextView = itemView.findViewById(R.id.filterLocation)
        private val dateTextView: TextView = itemView.findViewById(R.id.installationDate)
        private val maintenanceTextView: TextView = itemView.findViewById(R.id.nextMaintenance)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        private var currentFilter: Filter? = null

        init {
            itemView.setOnClickListener {
                currentFilter?.let { onFilterClick(it) }
            }

            deleteButton.setOnClickListener {
                currentFilter?.let { onDeleteClick(it) }
            }
        }

        fun bind(filter: Filter) {
            currentFilter = filter

            // Основная информация
            nameTextView.text = filter.name
            locationTextView.text = filter.location ?: "Местоположение не указано"

            // Форматирование даты
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val dateString = dateFormat.format(Date(filter.installationDate))
            dateTextView.text = "Установлен: $dateString"

            // Расчет следующего обслуживания (можно добавить логику позже)
            val nextMaintenanceText = when {
                filter.components.isNotEmpty() -> {
                    val nextDate = filter.components
                        .filter { it.nextReplacementDate != null }
                        .minByOrNull { it.nextReplacementDate!! }
                        ?.nextReplacementDate

                    if (nextDate != null) {
                        val nextDateString = dateFormat.format(Date(nextDate.toString()))
                        "Следующая замена: $nextDateString"
                    } else {
                        "Добавьте компоненты"
                    }
                }
                else -> {
                    "Добавьте компоненты"
                }
            }

            maintenanceTextView.text = nextMaintenanceText

            // Визуальное оформление статуса
            when {
                nextMaintenanceText.contains("Следующая замена") -> {
                    maintenanceTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.holo_blue_dark)
                    )
                }
                else -> {
                    maintenanceTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
                    )
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Filter>() {
            override fun areItemsTheSame(oldItem: Filter, newItem: Filter): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Filter, newItem: Filter): Boolean {
                return oldItem == newItem
            }
        }
    }
}