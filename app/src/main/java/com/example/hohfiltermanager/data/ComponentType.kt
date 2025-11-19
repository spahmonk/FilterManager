package com.example.hohfiltermanager.data

object ComponentType {
    val PREDFILTER = FilterComponent(
        id = 1,
        name = "Предфильтр PP",
        imageResId = android.R.drawable.ic_menu_report_image, // временная иконка
        lifespanMonths = 6,
        installationInstructions = "1. Перекройте воду\n2. Откройте кран для сброса давления\n3. Отсоедините колбу\n4. Замените картридж\n5. Соберите обратно",
        videoUrl = "https://youtube.com/предфильтр",
        purchaseUrl = "https://ozon.ru/предфильтр"
    )

    val CARBON_FILTER = FilterComponent(
        id = 2,
        name = "Угольный фильтр",
        imageResId = android.R.drawable.ic_menu_compass,
        lifespanMonths = 6,
        installationInstructions = "1. Перекройте воду\n2. Слейте остатки воды\n3. Замените угольный блок\n4. Промойте систему",
        videoUrl = "https://youtube.com/угольный",
        purchaseUrl = "https://wildberries.ru/угольный"
    )

    val MEMBRANE = FilterComponent(
        id = 3,
        name = "Мембрана",
        imageResId = android.R.drawable.ic_menu_agenda,
        lifespanMonths = 24,
        installationInstructions = "1. Демонтируйте старую мембрану\n2. Установите новую\n3. Проверьте герметичность",
        videoUrl = "https://youtube.com/мембрана",
        purchaseUrl = "https://aliexpress.ru/мембрана"
    )

    val POSTFILTER = FilterComponent(
        id = 4,
        name = "Постфильтр",
        imageResId = android.R.drawable.ic_menu_help,
        lifespanMonths = 12,
        installationInstructions = "1. Замените постфильтр\n2. Проверьте соединения\n3. Запустите воду",
        videoUrl = "https://youtube.com/постфильтр",
        purchaseUrl = "https://yandex.ru/постфильтр"
    )

    val ALL_COMPONENTS = listOf(PREDFILTER, CARBON_FILTER, MEMBRANE, POSTFILTER)

    fun getById(id: Long): FilterComponent? {
        return ALL_COMPONENTS.find { it.id == id }
    }
}