package com.example.hohfiltermanager.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterComponentDao {
    @Query("SELECT * FROM filter_components WHERE filterId = :filterId")
    fun getComponentsForFilter(filterId: Long): Flow<List<FilterComponentEntity>>

    @Query("SELECT * FROM filter_components")
    suspend fun getAllComponents(): List<FilterComponentEntity>

    @Insert
    suspend fun insertComponent(component: FilterComponentEntity): Long

    @Update
    suspend fun updateComponent(component: FilterComponentEntity)

    @Delete
    suspend fun deleteComponent(component: FilterComponentEntity)

    @Query("DELETE FROM filter_components WHERE filterId = :filterId")
    suspend fun deleteComponentsForFilter(filterId: Long)

    @Query("SELECT * FROM filter_components WHERE id = :componentId")
    suspend fun getComponentById(componentId: Long): FilterComponentEntity?
}