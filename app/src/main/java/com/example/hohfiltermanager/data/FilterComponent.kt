package com.example.hohfiltermanager.data

data class FilterComponent(
    val id: Long = 0,
    val componentTypeId: Long,
    val filterId: Long,
    val name: String,
    val imageResId: Int,
    val lifespanMonths: Int,
    val lastReplacementDate: Long = System.currentTimeMillis(),
    val nextReplacementDate: Long? = null,
    val installationInstructions: String = "",
    val videoUrl: String = "",
    val purchaseUrl: String = "",
    val isInstalled: Boolean = true,
    val customName: String? = null
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

    // ДОБАВЛЕННЫЙ МЕТОД: Получить количество дней до замены
    fun getDaysUntilReplacement(): Int {
        if (nextReplacementDate == null) return 0
        val diff = nextReplacementDate!! - System.currentTimeMillis()
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Проверить скоро ли замена
    fun isReplacementSoon(daysBefore: Int = 30): Boolean {
        val daysUntil = getDaysUntilReplacement()
        return daysUntil in 1..daysBefore
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Получить статус компонента
    fun getStatus(): String {
        return when {
            needsReplacement() -> "Требует замены"
            isReplacementSoon(7) -> "Срочная замена"
            isReplacementSoon(30) -> "Скоро замена"
            else -> "В норме"
        }
    }

    // ДОБАВЛЕННЫЙ МЕТОД: Получить цвет статуса
    fun getStatusColorResId(): Int {
        return when {
            needsReplacement() -> android.R.color.holo_red_dark
            isReplacementSoon(7) -> android.R.color.holo_orange_dark
            isReplacementSoon(30) -> android.R.color.holo_orange_light
            else -> android.R.color.holo_green_dark
        }
    }
}