package com.example.hohfiltermanager.data

data class Filter(
    val id: Long = 0,
    val name: String,
    val location: String,
    val installationDate: Long = System.currentTimeMillis(),
    val components: List<FilterComponent> = emptyList(),
    val hasAccumulatorTank: Boolean = false
)