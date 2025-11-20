package com.example.hohfiltermanager.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hohfiltermanager.data.FilterComponent
import com.example.hohfiltermanager.R
import java.text.SimpleDateFormat
import java.util.*

class ComponentAdapter(
    private val onComponentClick: (FilterComponent) -> Unit,
    private val onReplaceClick: (FilterComponent) -> Unit,
    private val onDeleteClick: (FilterComponent) -> Unit
) : ListAdapter<FilterComponent, ComponentAdapter.ComponentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component, parent, false)
        return ComponentViewHolder(view, onComponentClick, onReplaceClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ComponentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ComponentViewHolder(
        itemView: View,
        private val onComponentClick: (FilterComponent) -> Unit,
        private val onReplaceClick: (FilterComponent) -> Unit,
        private val onDeleteClick: (FilterComponent) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val iconImage: ImageView = itemView.findViewById(R.id.componentIcon)
        private val nameText: TextView = itemView.findViewById(R.id.componentName)
        private val statusText: TextView = itemView.findViewById(R.id.componentStatus)
        private val lastReplacementText: TextView = itemView.findViewById(R.id.lastReplacementDate)
        private val nextReplacementText: TextView = itemView.findViewById(R.id.nextReplacementDate)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.replacementProgress)
        private val replaceButton: Button = itemView.findViewById(R.id.replaceButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        private var currentComponent: FilterComponent? = null

        init {
            itemView.setOnClickListener {
                currentComponent?.let { onComponentClick(it) }
            }

            replaceButton.setOnClickListener {
                currentComponent?.let { onReplaceClick(it) }
            }

            deleteButton.setOnClickListener {
                currentComponent?.let { onDeleteClick(it) }
            }
        }

        fun bind(component: FilterComponent) {
            currentComponent = component

            iconImage.setImageResource(component.imageResId)
            nameText.text = component.name

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            lastReplacementText.text = "Заменен: ${dateFormat.format(Date(component.lastReplacementDate))}"

            if (component.nextReplacementDate != null) {
                nextReplacementText.text = "Следующая замена: ${dateFormat.format(Date(component.nextReplacementDate!!))}"

                val progress = component.getProgressPercentage()
                progressBar.progress = progress

                when {
                    component.needsReplacement() -> {
                        statusText.text = "🚨 ТРЕБУЕТ ЗАМЕНЫ!"
                        statusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                        replaceButton.isEnabled = true
                        replaceButton.alpha = 1.0f
                    }
                    component.isReplacementSoon(30) -> {
                        val daysLeft = component.getDaysUntilReplacement()
                        statusText.text = "⚠️ Скоро замена ($daysLeft дней)"
                        statusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_orange_dark))
                        replaceButton.isEnabled = true
                        replaceButton.alpha = 1.0f
                    }
                    else -> {
                        statusText.text = "✅ В норме ($progress%)"
                        statusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                        replaceButton.isEnabled = false
                        replaceButton.alpha = 0.5f
                    }
                }
            } else {
                nextReplacementText.text = "Срок замены не установлен этот!!!"
                progressBar.progress = 0
                statusText.text = "🆕 Новый компонент"
                statusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
                replaceButton.isEnabled = true
                replaceButton.alpha = 1.0f
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FilterComponent>() {
            override fun areItemsTheSame(oldItem: FilterComponent, newItem: FilterComponent): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FilterComponent, newItem: FilterComponent): Boolean {
                return oldItem == newItem
            }
        }
    }
}