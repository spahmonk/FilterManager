package com.example.hohfiltermanager.data

data class Filter(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val location: String,
    val installationDate: Long,
    val components: List<FilterComponent> = emptyList()
)