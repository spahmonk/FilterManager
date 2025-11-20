package com.example.hohfiltermanager.data

import com.example.hohfiltermanager.data.local.AppDatabase
import com.example.hohfiltermanager.data.local.FilterComponentEntity
import com.example.hohfiltermanager.data.local.FilterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FilterRepository(private val database: AppDatabase) {

    // Упрощенная версия - только фильтры без компонентов
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

    // Получить фильтр по ID
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
            database.filterDao().insertComponent(componentEntity)
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
        database.filterDao().insertComponent(componentEntity)
        return 0 // Заглушка, надо будет исправить
    }

    // Удалить фильтр
    suspend fun deleteFilter(filter: Filter) {
        //database.filterComponentDao().deleteComponentsForFilter(filter.id)
        database.filterDao().deleteFilter(FilterEntity(
            id = filter.id,
            name = filter.name,
            location = filter.location,
            installationDate = filter.installationDate
        ))
    }

    // Получить компоненты для фильтра (упрощенная версия)
    suspend fun getComponentsForFilter(filterId: Long): List<FilterComponent> {
        // Временное решение - возвращаем пустой список
        return emptyList()
    }

    suspend fun getFilterWithComponents(filterId: Long): Filter? {
        val filterEntity = database.filterDao().getFilterById(filterId) ?: return null

        // Временное решение - возвращаем фильтр с тестовыми компонентами
        val testComponents = listOf(
            ComponentType.PREDFILTER.copy(
                id = 1,
                filterId = filterId,
                lastReplacementDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 150),
                nextReplacementDate = ComponentType.calculateNextReplacement(ComponentType.PREDFILTER, System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 150))
            ),
            ComponentType.CARBON_FILTER.copy(
                id = 2,
                filterId = filterId,
                lastReplacementDate = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 120),
                nextReplacementDate = ComponentType.calculateNextReplacement(ComponentType.CARBON_FILTER, System.currentTimeMillis() - (1000L * 60 * 60 * 24 * 120))
            )
        )

        return Filter(
            id = filterEntity.id,
            name = filterEntity.name,
            location = filterEntity.location,
            installationDate = filterEntity.installationDate,
            components = testComponents,
            hasAccumulatorTank = testComponents.any {
                it.componentTypeId == ComponentType.ACCUMULATOR_TANK.componentTypeId
            }
        )
    }

    suspend fun updateComponentReplacement(componentId: Long, replacementDate: Long) {
        //val componentEntity = database.filterComponentDao().getComponentById(componentId)
        //componentEntity?.let {
        //    val updatedEntity = it.copy(lastReplacementDate = replacementDate)
        //    database.filterComponentDao().updateComponent(updatedEntity)
        //}
    }
}