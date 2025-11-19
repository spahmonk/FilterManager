package com.example.hohfiltermanager.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {
    @Query("SELECT * FROM filters")
    fun getAllFilters(): Flow<List<FilterEntity>>

    @Insert
    suspend fun insertFilter(filter: FilterEntity): Long

    @Delete
    suspend fun deleteFilter(filter: FilterEntity)

    @Query("DELETE FROM filters")
    suspend fun deleteAll()
}