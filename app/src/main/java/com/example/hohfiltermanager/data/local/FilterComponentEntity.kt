package com.example.hohfiltermanager.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "filter_components",
    foreignKeys = [ForeignKey(
        entity = FilterEntity::class,
        parentColumns = ["id"],
        childColumns = ["filterId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FilterComponentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filterId: Long,
    val componentTypeId: Long,
    val customName: String? = null,
    val lastReplacementDate: Long = System.currentTimeMillis(),
    val isInstalled: Boolean = true
)