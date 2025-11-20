package com.example.hohfiltermanager.data

data class FilterComponent(
    val id: Long = 0,
    val componentTypeId: Long, // Ссылка на тип компонента
    val filterId: Long, // Ссылка на фильтр
    val name: String,
    val imageResId: Int,
    val lifespanMonths: Int,
    val lastReplacementDate: Long = System.currentTimeMillis(),
    val nextReplacementDate: Long? = null,
    val installationInstructions: String = "",
    val videoUrl: String = "",
    val purchaseUrl: String = "",
    val isInstalled: Boolean = true,
    val customName: String? = null // Для кастомных названий
) {
    fun calculateNextReplacement(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = lastReplacementDate
        calendar.add(java.util.Calendar.MONTH, lifespanMonths)
        return calendar.timeInMillis
    }

    fun needsReplacement(): Boolean {
        return nextReplacementDate?.let { it < System.currentTimeMillis() } ?: false
    }

    fun getProgressPercentage(): Int {
        if (nextReplacementDate == null) return 0

        val total = nextReplacementDate!! - lastReplacementDate
        val passed = System.currentTimeMillis() - lastReplacementDate

        if (passed >= total) return 100
        return ((passed.toDouble() / total.toDouble()) * 100).toInt()
    }
}