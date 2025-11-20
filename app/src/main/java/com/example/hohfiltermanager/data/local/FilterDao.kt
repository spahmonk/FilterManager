package com.example.hohfiltermanager.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterDao {
    @Query("SELECT * FROM filters ORDER BY installationDate DESC")
    fun getAllFilters(): Flow<List<FilterEntity>>

    @Query("SELECT * FROM filters WHERE id = :filterId")
    suspend fun getFilterById(filterId: Long): FilterEntity?

    @Insert
    suspend fun insertFilter(filter: FilterEntity): Long

    @Update
    suspend fun updateFilter(filter: FilterEntity)

    @Delete
    suspend fun deleteFilter(filter: FilterEntity)

    @Query("DELETE FROM filters")
    suspend fun deleteAll()
}