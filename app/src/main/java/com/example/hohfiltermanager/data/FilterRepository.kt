package com.example.hohfiltermanager.data

import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.data.local.FilterComponentEntity
import com.example.hohfiltermanager.data.local.FilterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilterRepository(private val database: AppDatabase) {

    // Упрощенная версия - получаем только фильтры
    fun getAllFilters(): Flow<List<Filter>> {
        return database.filterDao().getAllFilters().map { filters ->
            filters.map { entity ->
                Filter(
                    id = entity.id,
                    name = entity.name,
                    location = entity.location,
                    installationDate = entity.installationDate,
                    components = emptyList(), // Пока пустой список
                    hasAccumulatorTank = false
                )
            }
        }
    }

    // Получить фильтр с компонентами
    suspend fun getFilterWithComponents(filterId: Long): Filter? {
        val filterEntity = database.filterDao().getFilterById(filterId) ?: return null
        val components = getComponentsForFilter(filterId)

        return Filter(
            id = filterEntity.id,
            name = filterEntity.name,
            location = filterEntity.location,
            installationDate = filterEntity.installationDate,
            components = components,
            hasAccumulatorTank = components.any {
                it.componentTypeId == ComponentType.ACCUMULATOR_TANK.componentTypeId
            }
        )
    }

    // Получить компоненты для фильтра
    suspend fun getComponentsForFilter(filterId: Long): List<FilterComponent> {
        val componentEntities = database.filterComponentDao().getComponentsForFilter(filterId)
        // Временное решение - нужно исправить Flow
        return emptyList<FilterComponent>()
    }

    // Добавить фильтр с компонентами
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

    // Получить все компоненты (для отладки)
    suspend fun getAllComponents(): List<FilterComponentEntity> {
        return database.filterComponentDao().getAllComponents()
    }
}