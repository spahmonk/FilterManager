package com.example.hohfiltermanager.data

import java.util.Date

data class FilterComponent(
    val id: Long,
    val name: String,
    val imageResId: Int,
    val lifespanMonths: Int,
    val lastReplacementDate: Date? = null,
    val nextReplacementDate: Date? = null,
    val installationInstructions: String = "",
    val videoUrl: String = "",
    val purchaseUrl: String = "",
    val isInstalled: Boolean = false
) {
    fun calculateNextReplacement(): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = lastReplacementDate ?: Date()
        calendar.add(java.util.Calendar.MONTH, lifespanMonths)
        return calendar.time
    }

    fun needsReplacement(): Boolean {
        return nextReplacementDate?.before(Date()) ?: false
    }
}