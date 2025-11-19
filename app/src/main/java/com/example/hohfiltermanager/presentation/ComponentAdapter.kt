package com.example.hohfiltermanager.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hohfiltermanager.data.FilterComponent
import com.example.hohfiltermanager.R
import java.text.SimpleDateFormat
import java.util.*

class ComponentAdapter(
    private val onComponentClick: (FilterComponent) -> Unit,
    private val onReplaceClick: (FilterComponent) -> Unit
) : RecyclerView.Adapter<ComponentAdapter.ComponentViewHolder>() {

    private var components: List<FilterComponent> = emptyList()

    fun submitList(newComponents: List<FilterComponent>) {
        this.components = newComponents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_component, parent, false)
        return ComponentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComponentViewHolder, position: Int) {
        holder.bind(components[position])
    }

    override fun getItemCount(): Int = components.size

    inner class ComponentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImage: ImageView = itemView.findViewById(R.id.componentIcon)
        private val nameText: TextView = itemView.findViewById(R.id.componentName)
        private val statusText: TextView = itemView.findViewById(R.id.componentStatus)
        private val lastReplacementText: TextView = itemView.findViewById(R.id.lastReplacementDate)
        private val nextReplacementText: TextView = itemView.findViewById(R.id.nextReplacementDate)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.replacementProgress)
        private val replaceButton: Button = itemView.findViewById(R.id.replaceButton)

        fun bind(component: FilterComponent) {
            // Устанавливаем иконку (временно используем системные)
            iconImage.setImageResource(component.imageResId)

            nameText.text = component.name

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

            // Дата последней замены
            if (component.lastReplacementDate != null) {
                lastReplacementText.text = "Заменен: ${dateFormat.format(component.lastReplacementDate)}"
            } else {
                lastReplacementText.text = "Еще не заменялся"
            }

            // Дата следующей замены и прогресс
            if (component.nextReplacementDate != null) {
                nextReplacementText.text = "Следующая замена: ${dateFormat.format(component.nextReplacementDate)}"

                // Расчет прогресса до замены
                val progress = calculateReplacementProgress(component)
                progressBar.progress = progress

                // Статус компонента
                if (component.needsReplacement()) {
                    statusText.text = "ТРЕБУЕТ ЗАМЕНЫ!"
                    statusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
                } else {
                    statusText.text = "В норме ($progress%)"
                    statusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_green_dark))
                }
            } else {
                nextReplacementText.text = "Срок замены не установлен"
                progressBar.progress = 0
                statusText.text = "Новый"
                statusText.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
            }

            // Кнопка замены
            replaceButton.setOnClickListener {
                onReplaceClick(component)
            }

            // Клик по всему элементу
            itemView.setOnClickListener {
                onComponentClick(component)
            }
        }

        private fun calculateReplacementProgress(component: FilterComponent): Int {
            val lastDate = component.lastReplacementDate ?: return 0
            val nextDate = component.nextReplacementDate ?: return 0
            val currentDate = Date()

            if (currentDate.after(nextDate)) return 100
            if (currentDate.before(lastDate)) return 0

            val totalDuration = nextDate.time - lastDate.time
            val elapsedDuration = currentDate.time - lastDate.time

            return ((elapsedDuration.toDouble() / totalDuration.toDouble()) * 100).toInt()
        }
    }
}