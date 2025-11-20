package com.example.hohfiltermanager.data

object ComponentType {
    // Предфильтр механической очистки
    val PREDFILTER = FilterComponent(
        componentTypeId = 1,
        filterId = 0, // Будет установлен при добавлении к фильтру
        name = "Предфильтр PP",
        imageResId = android.R.drawable.ic_menu_report_image,
        lifespanMonths = 6,
        lastReplacementDate = 0L, // Long вместо Date
        nextReplacementDate = null,
        installationInstructions = "1. Перекройте воду\n2. Откройте кран для сброса давления\n3. Отсоедините колбу\n4. Замените картридж\n5. Соберите обратно\n6. Проверьте герметичность",
        videoUrl = "https://youtube.com/предфильтр",
        purchaseUrl = "https://ozon.ru/предфильтр"
    )

    // Угольный фильтр
    val CARBON_FILTER = FilterComponent(
        componentTypeId = 2,
        filterId = 0,
        name = "Угольный фильтр",
        imageResId = android.R.drawable.ic_menu_compass,
        lifespanMonths = 6,
        lastReplacementDate = 0L,
        nextReplacementDate = null,
        installationInstructions = "1. Перекройте воду\n2. Слейте остатки воды\n3. Отсоедините колбу\n4. Замените угольный блок\n5. Промойте систему 10 минут\n6. Проверьте качество воды",
        videoUrl = "https://youtube.com/угольный-фильтр",
        purchaseUrl = "https://wildberries.ru/угольный-фильтр"
    )

    // Мембрана обратного осмоса
    val MEMBRANE = FilterComponent(
        componentTypeId = 3,
        filterId = 0,
        name = "Мембрана обратного осмоса",
        imageResId = android.R.drawable.ic_menu_agenda,
        lifespanMonths = 24,
        lastReplacementDate = 0L,
        nextReplacementDate = null,
        installationInstructions = "1. Демонтируйте старую мембрану\n2. Установите новую мембрану\n3. Проверьте направление установки\n4. Запустите воду на 30 минут\n5. Проверьте производительность системы",
        videoUrl = "https://youtube.com/мембрана-осмос",
        purchaseUrl = "https://aliexpress.ru/мембрана-осмос"
    )

    // Постфильтр минерализатор
    val POSTFILTER = FilterComponent(
        componentTypeId = 4,
        filterId = 0,
        name = "Постфильтр-минерализатор",
        imageResId = android.R.drawable.ic_menu_help,
        lifespanMonths = 12,
        lastReplacementDate = 0L,
        nextReplacementDate = null,
        installationInstructions = "1. Замените постфильтр\n2. Проверьте соединения\n3. Запустите воду\n4. Проверьте вкус воды",
        videoUrl = "https://youtube.com/постфильтр",
        purchaseUrl = "https://yandex.ru/постфильтр"
    )

    // Бак-накопитель
    val ACCUMULATOR_TANK = FilterComponent(
        componentTypeId = 5,
        filterId = 0,
        name = "Бак-накопитель",
        imageResId = android.R.drawable.ic_menu_upload,
        lifespanMonths = 60, // 5 лет
        lastReplacementDate = 0L,
        nextReplacementDate = null,
        installationInstructions = "1. Проверьте давление в баке (0.5-0.7 атм)\n2. При необходимости подкачайте воздух\n3. Проверьте герметичность соединений\n4. При повреждении замените бак\n5. Проверьте работу автоматики",
        videoUrl = "https://youtube.com/бак-накопитель",
        purchaseUrl = "https://ozon.ru/бак-накопитель"
    )

    // УФ-лампа (дополнительный компонент)
    val UV_LAMP = FilterComponent(
        componentTypeId = 6,
        filterId = 0,
        name = "УФ-лампа обеззараживания",
        imageResId = android.R.drawable.ic_menu_gallery,
        lifespanMonths = 12,
        lastReplacementDate = 0L,
        nextReplacementDate = null,
        installationInstructions = "1. Отключите питание\n2. Извлеките старую лампу\n3. Установите новую лампу\n4. Проверьте герметичность камеры\n5. Включите питание",
        videoUrl = "https://youtube.com/уф-лампа",
        purchaseUrl = "https://wildberries.ru/уф-лампа"
    )

    // Ионообменный фильтр (для умягчения)
    val ION_EXCHANGE = FilterComponent(
        componentTypeId = 7,
        filterId = 0,
        name = "Ионообменный фильтр",
        imageResId = android.R.drawable.ic_menu_manage,
        lifespanMonths = 12,
        lastReplacementDate = 0L,
        nextReplacementDate = null,
        installationInstructions = "1. Замените ионообменную смолу\n2. Проведите регенерацию\n3. Промойте систему\n4. Проверьте жесткость воды",
        videoUrl = "https://youtube.com/ионообменный-фильтр",
        purchaseUrl = "https://aliexpress.ru/ионообменный-фильтр"
    )

    // Все компоненты в списке
    val ALL_COMPONENTS = listOf(
        PREDFILTER,
        CARBON_FILTER,
        MEMBRANE,
        POSTFILTER,
        ACCUMULATOR_TANK,
        UV_LAMP,
        ION_EXCHANGE
    )

    // Получить компонент по ID
    fun getById(id: Long): FilterComponent? {
        return ALL_COMPONENTS.find { it.componentTypeId == id }
    }

    // Получить компоненты по категориям
    fun getPreFilters(): List<FilterComponent> {
        return listOf(PREDFILTER, CARBON_FILTER)
    }

    fun getMainFilters(): List<FilterComponent> {
        return listOf(MEMBRANE)
    }

    fun getPostFilters(): List<FilterComponent> {
        return listOf(POSTFILTER, UV_LAMP, ION_EXCHANGE)
    }

    fun getSystemComponents(): List<FilterComponent> {
        return listOf(ACCUMULATOR_TANK)
    }

    // Получить компоненты для определенного типа системы
    fun getComponentsForSystem(systemType: String): List<FilterComponent> {
        return when (systemType) {
            "BASIC" -> listOf(PREDFILTER, CARBON_FILTER)
            "OSMOSIS" -> listOf(PREDFILTER, CARBON_FILTER, MEMBRANE, POSTFILTER)
            "FULL" -> ALL_COMPONENTS
            else -> emptyList()
        }
    }

    // Получить рекомендуемый срок замены в текстовом формате
    fun getLifespanText(component: FilterComponent): String {
        return when (component.lifespanMonths) {
            in 1..6 -> "Каждые ${component.lifespanMonths} месяцев"
            in 7..12 -> "Каждый год"
            in 13..24 -> "Каждые 2 года"
            else -> "Каждые ${component.lifespanMonths / 12} лет"
        }
    }

    // Получить компоненты которые скоро требуют замены (для уведомлений)
    fun getComponentsNeedingReplacement(components: List<FilterComponent>, daysBefore: Int = 7): List<FilterComponent> {
        val currentTime = System.currentTimeMillis()
        return components.filter { component ->
            component.nextReplacementDate?.let { nextDate ->
                val timeUntilReplacement = nextDate - currentTime
                val daysUntilReplacement = timeUntilReplacement / (1000 * 60 * 60 * 24)
                daysUntilReplacement <= daysBefore
            } ?: false
        }
    }
}