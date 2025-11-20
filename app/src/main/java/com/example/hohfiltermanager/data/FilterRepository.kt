package com.example.hohfiltermanager.data

import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.data.local.FilterComponentEntity
import com.example.hohfiltermanager.data.local.FilterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FilterRepository(private val database: AppDatabase) {

    // Получить все фильтры с их компонентами
    fun getAllFiltersWithComponents(): Flow<List<Filter>> {
        return database.filterDao().getAllFilters().combine(
            database.filterComponentDao().getAllComponents()
        ) { filters, allComponents ->
            filters.map { filterEntity ->
                val filterComponents = allComponents
                    .filter { it.filterId == filterEntity.id }
                    .map { componentEntity ->
                        val componentType = ComponentType.getById(componentEntity.componentTypeId)
                        componentType?.copy(
                            id = componentEntity.id,
                            filterId = componentEntity.filterId,
                            lastReplacementDate = componentEntity.lastReplacementDate,
                            isInstalled = componentEntity.isInstalled,
                            customName = componentEntity.customName,
                            nextReplacementDate = componentType.calculateNextReplacement()
                        ) ?: throw IllegalStateException("Unknown component type: ${componentEntity.componentTypeId}")
                    }

                Filter(
                    id = filterEntity.id,
                    name = filterEntity.name,
                    location = filterEntity.location,
                    installationDate = filterEntity.installationDate,
                    components = filterComponents,
                    hasAccumulatorTank = filterComponents.any { it.componentTypeId == ComponentType.ACCUMULATOR_TANK.componentTypeId }
                )
            }
        }
    }

    // Добавить фильтр
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
            database.filterComponentDao().insertComponent(componentEntity)
        }

        return filterId
    }

    // Добавить компонент к фильтру
    suspend fun addComponentToFilter(filterId: Long, component: FilterComponent): Long {
        val componentEntity = FilterComponentEntity(
            filterId = filterId,
            componentTypeId = component.componentTypeId,
            customName = component.customName,
            lastReplacementDate = component.lastReplacementDate,
            isInstalled = component.isInstalled
        )
        return database.filterComponentDao().insertComponent(componentEntity)
    }

    // Обновить замену компонента
    suspend fun updateComponentReplacement(componentId: Long, replacementDate: Long) {
        val componentEntity = database.filterComponentDao().getComponentById(componentId)
        componentEntity?.let {
            val updatedEntity = it.copy(lastReplacementDate = replacementDate)
            database.filterComponentDao().updateComponent(updatedEntity)
        }
    }

    // Удалить фильтр и его компоненты
    suspend fun deleteFilter(filter: Filter) {
        database.filterComponentDao().deleteComponentsForFilter(filter.id)
        database.filterDao().deleteFilter(FilterEntity(
            id = filter.id,
            name = filter.name,
            location = filter.location,
            installationDate = filter.installationDate
        ))
    }
}