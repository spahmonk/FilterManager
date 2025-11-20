package com.example.hohfiltermanager.data

object ComponentType {
    val PREDFILTER = FilterComponent(
        componentTypeId = 1,
        filterId = 0, // Будет установлено при добавлении к фильтру
        name = "Предфильтр PP",
        imageResId = android.R.drawable.ic_menu_report_image,
        lifespanMonths = 6,
        installationInstructions = "1. Перекройте воду\n2. Откройте кран для сброса давления\n3. Отсоедините колбу\n4. Замените картридж\n5. Соберите обратно",
        videoUrl = "https://youtube.com/предфильтр",
        purchaseUrl = "https://ozon.ru/предфильтр"
    )

    val CARBON_FILTER = FilterComponent(
        componentTypeId = 2,
        filterId = 0,
        name = "Угольный фильтр",
        imageResId = android.R.drawable.ic_menu_compass,
        lifespanMonths = 6,
        installationInstructions = "1. Перекройте воду\n2. Слейте остатки воды\n3. Замените угольный блок\n4. Промойте систему",
        videoUrl = "https://youtube.com/угольный",
        purchaseUrl = "https://wildberries.ru/угольный"
    )

    val MEMBRANE = FilterComponent(
        componentTypeId = 3,
        filterId = 0,
        name = "Мембрана",
        imageResId = android.R.drawable.ic_menu_agenda,
        lifespanMonths = 24,
        installationInstructions = "1. Демонтируйте старую мембрану\n2. Установите новую\n3. Проверьте герметичность",
        videoUrl = "https://youtube.com/мембрана",
        purchaseUrl = "https://aliexpress.ru/мембрана"
    )

    val POSTFILTER = FilterComponent(
        componentTypeId = 4,
        filterId = 0,
        name = "Постфильтр",
        imageResId = android.R.drawable.ic_menu_help,
        lifespanMonths = 12,
        installationInstructions = "1. Замените постфильтр\n2. Проверьте соединения\n3. Запустите воду",
        videoUrl = "https://youtube.com/постфильтр",
        purchaseUrl = "https://yandex.ru/постфильтр"
    )

    // НОВЫЙ: Бак-накопитель
    val ACCUMULATOR_TANK = FilterComponent(
        componentTypeId = 5,
        filterId = 0,
        name = "Бак-накопитель",
        imageResId = android.R.drawable.ic_menu_upload,
        lifespanMonths = 60, // 5 лет
        installationInstructions = "1. Проверьте давление в баке (должно быть 0.5-0.7 атм)\n2. При необходимости подкачайте воздух\n3. Проверьте герметичность соединений\n4. При повреждении замените бак",
        videoUrl = "https://youtube.com/бак-накопитель",
        purchaseUrl = "https://ozon.ru/бак-накопитель"
    )

    val MINERALIZER = FilterComponent(
        componentTypeId = 6,
        filterId = 0,
        name = "Минерализатор",
        imageResId = android.R.drawable.ic_menu_share,
        lifespanMonths = 12,
        installationInstructions = "1. Замените минерализатор\n2. Проверьте соединения",
        videoUrl = "https://youtube.com/минерализатор",
        purchaseUrl = "https://wildberries.ru/минерализатор"
    )

    val ALL_COMPONENTS = listOf(
        PREDFILTER,
        CARBON_FILTER,
        MEMBRANE,
        POSTFILTER,
        ACCUMULATOR_TANK,
        MINERALIZER
    )

    fun getById(id: Long): FilterComponent? {
        return ALL_COMPONENTS.find { it.componentTypeId == id }
    }
}