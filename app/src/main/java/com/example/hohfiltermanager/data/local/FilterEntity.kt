package com.example.hohfiltermanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filters")
data class FilterEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val location: String,
    val installationDate: Long
)