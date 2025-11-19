package com.example.hohfiltermanager.data

object FilterDataSource {
    private val filters = mutableListOf<Filter>()

    fun getFilters(): List<Filter> = filters

    fun addFilter(filter: Filter) {
        filters.add(filter)
    }

    fun deleteFilter(filter: Filter) {
        filters.remove(filter)
    }
}