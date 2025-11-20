package com.example.hohfiltermanager.data

import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.data.local.FilterComponentEntity
import com.example.hohfiltermanager.data.local.FilterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilterRepository(private val database: AppDatabase) {

    fun getAllFilters(): Flow<List<Filter>> {
        return database.filterDao().getAllFilters().map { filters ->
            filters.map { entity ->
                Filter(
                    id = entity.id,
                    name = entity.name,
                    location = entity.location,
                    installationDate = entity.installationDate,
                    components = emptyList(),
                    hasAccumulatorTank = false
                )
            }
        }
    }

    suspend fun getFilterById(filterId: Long): Filter? {
        val filterEntity = database.filterDao().getFilterById(filterId) ?: return null
        return Filter(
            id = filterEntity.id,
            name = filterEntity.name,
            location = filterEntity.location,
            installationDate = filterEntity.installationDate,
            components = emptyList(),
            hasAccumulatorTank = false
        )
    }

    suspend fun addFilter(filter: FilterEntity, components: List<FilterComponent>): Long {
        val filterId = database.filterDao().insertFilter(filter)
        components.forEach { component ->
            val componentEntity = FilterComponentEntity(
                filterId = filterId,
                componentTypeId = component.componentTypeId,
                customName = component.customName,
                lastReplacementDate = component.lastReplacementDate,
                isInstalled = component.isInstalled
            )
            database.filterDao().insertComponent(componentEntity)
        }
        return filterId
    }

    suspend fun addComponentToFilter(filterId: Long, component: FilterComponent): Long {
        val componentEntity = FilterComponentEntity(
            filterId = filterId,
            componentTypeId = component.componentTypeId,
            customName = component.customName,
            lastReplacementDate = component.lastReplacementDate,
            isInstalled = component.isInstalled
        )
        return database.filterDao().insertComponent(componentEntity)
    }

    suspend fun deleteFilter(filter: Filter) {
        database.filterDao().deleteComponentsForFilter(filter.id)
        database.filterDao().deleteFilter(FilterEntity(
            id = filter.id,
            name = filter.name,
            location = filter.location,
            installationDate = filter.installationDate
        ))
    }

    suspend fun getComponentsForFilter(filterId: Long): List<FilterComponent> {
        return database.filterDao().getComponentsForFilter(filterId).map { toFilterComponent(it) }
    }

    suspend fun getFilterWithComponents(filterId: Long): Filter? {
        val filterEntity = database.filterDao().getFilterById(filterId) ?: return null
        val components = getComponentsForFilter(filterId)

        return Filter(
            id = filterEntity.id,
            name = filterEntity.name,
            location = filterEntity.location,
            installationDate = filterEntity.installationDate,
            components = components,
            hasAccumulatorTank = components.any { it.componentTypeId == ComponentType.ACCUMULATOR_TANK.componentTypeId }
        )
    }

    suspend fun updateComponentReplacement(componentId: Long, replacementDate: Long) {
        val componentEntity = database.filterDao().getComponentById(componentId)
        componentEntity?.let {
            val updatedEntity = it.copy(lastReplacementDate = replacementDate)
            database.filterDao().updateComponent(updatedEntity)
        }
    }

    suspend fun deleteComponent(componentId: Long) {
        database.filterDao().deleteComponentById(componentId)
    }

    private fun toFilterComponent(entity: FilterComponentEntity): FilterComponent {
        val componentType = ComponentType.getById(entity.componentTypeId)
        return componentType!!.copy(
            id = entity.id,
            filterId = entity.filterId,
            customName = entity.customName,
            lastReplacementDate = entity.lastReplacementDate,
            isInstalled = entity.isInstalled
        )
    }
}