package com.example.hohfiltermanager.data

data class Filter(
    val id: Long = 0,
    val name: String,
    val location: String,
    val installationDate: Long = System.currentTimeMillis(),
    val components: List<FilterComponent> = emptyList(),
    val hasAccumulatorTank: Boolean = false
) {
    fun getNextMaintenanceDate(): Long? {
        return components
            .filter { it.nextReplacementDate != null }
            .minByOrNull { it.nextReplacementDate!! }
            ?.nextReplacementDate
    }

    fun getComponentsNeedingReplacement(): List<FilterComponent> {
        return components.filter { it.needsReplacement() }
    }

    fun getComponentsReplacementSoon(daysBefore: Int = 30): List<FilterComponent> {
        return components.filter { it.isReplacementSoon(daysBefore) }
    }

    fun getMaintenanceStatus(): String {
        val needsReplace = getComponentsNeedingReplacement().size
        val soonReplace = getComponentsReplacementSoon().size

        return when {
            needsReplace > 0 -> "Требует внимания ($needsReplace компонентов)"
            soonReplace > 0 -> "Скоро замена ($soonReplace компонентов)"
            else -> "В норме"
        }
    }
}