package com.example.hohfiltermanager

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hohfiltermanager.data.ComponentType
import java.text.SimpleDateFormat
import java.util.*

class ComponentDetailsActivity : AppCompatActivity() {

    private lateinit var componentNameText: TextView
    private lateinit var instructionsText: TextView
    private lateinit var lastReplacementText: TextView
    private lateinit var nextReplacementText: TextView
    private lateinit var statusText: TextView
    private lateinit var watchVideoButton: Button
    private lateinit var buyButton: Button
    private lateinit var confirmReplacementButton: Button

    private var componentId: Long = -1
    private var componentName: String = ""
    private var installationInstructions: String = ""
    private var videoUrl: String = ""
    private var purchaseUrl: String = ""
    private var lastReplacementDate: Date? = null
    private var nextReplacementDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_component_details)

        // Получаем данные из Intent
        getIntentData()

        // Инициализация views
        initializeViews()
        setupUI()
        setupClickListeners()
    }

    private fun getIntentData() {
        componentId = intent.getLongExtra("component_id", -1)
        componentName = intent.getStringExtra("component_name") ?: "Компонент"
        installationInstructions = intent.getStringExtra("installation_instructions") ?: "Инструкция не доступна"
        videoUrl = intent.getStringExtra("video_url") ?: ""
        purchaseUrl = intent.getStringExtra("purchase_url") ?: ""

        val lastReplacementMillis = intent.getLongExtra("last_replacement_date", -1)
        val nextReplacementMillis = intent.getLongExtra("next_replacement_date", -1)

        if (lastReplacementMillis != -1L) {
            lastReplacementDate = Date(lastReplacementMillis)
        }
        if (nextReplacementMillis != -1L) {
            nextReplacementDate = Date(nextReplacementMillis)
        }
    }

    private fun initializeViews() {
        componentNameText = findViewById(R.id.componentNameText)
        instructionsText = findViewById(R.id.instructionsText)
        lastReplacementText = findViewById(R.id.lastReplacementText)
        nextReplacementText = findViewById(R.id.nextReplacementText)
        statusText = findViewById(R.id.statusText)
        watchVideoButton = findViewById(R.id.watchVideoButton)
        buyButton = findViewById(R.id.buyButton)
        confirmReplacementButton = findViewById(R.id.confirmReplacementButton)
    }

    private fun setupUI() {
        componentNameText.text = componentName
        title = componentName

        // Инструкции
        instructionsText.text = installationInstructions

        // Даты замен
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        if (lastReplacementDate != null) {
            lastReplacementText.text = "Последняя замена: ${dateFormat.format(lastReplacementDate)}"
        } else {
            lastReplacementText.text = "Еще не заменялся"
        }

        if (nextReplacementDate != null) {
            nextReplacementText.text = "Следующая замена: ${dateFormat.format(nextReplacementDate)}"

            // Статус компонента
            val needsReplacement = nextReplacementDate!!.before(Date())
            if (needsReplacement) {
                statusText.text = "❌ ТРЕБУЕТ ЗАМЕНЫ"
                statusText.setBackgroundColor(getColor(android.R.color.holo_red_light))
                confirmReplacementButton.isEnabled = true
                confirmReplacementButton.alpha = 1.0f
            } else {
                statusText.text = "✅ В НОРМЕ"
                statusText.setBackgroundColor(getColor(android.R.color.holo_green_light))
                confirmReplacementButton.isEnabled = false
                confirmReplacementButton.alpha = 0.5f
            }
        } else {
            nextReplacementText.text = "Срок замены не установлен"
            statusText.text = "ℹ️ НОВЫЙ КОМПОНЕНТ"
            statusText.setBackgroundColor(getColor(android.R.color.darker_gray))
            confirmReplacementButton.isEnabled = true
            confirmReplacementButton.alpha = 1.0f
        }

        // Скрываем кнопки если нет ссылок
        if (videoUrl.isEmpty()) {
            watchVideoButton.visibility = android.view.View.GONE
        }

        if (purchaseUrl.isEmpty()) {
            buyButton.visibility = android.view.View.GONE
        }
    }

    private fun setupClickListeners() {
        // Кнопка "Назад"
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Кнопка "Смотреть видео"
        watchVideoButton.setOnClickListener {
            openVideo()
        }

        // Кнопка "Купить"
        buyButton.setOnClickListener {
            openPurchaseLink()
        }

        // Кнопка "Подтвердить замену"
        confirmReplacementButton.setOnClickListener {
            confirmReplacement()
        }

        // Кнопка "Поделиться инструкцией"
        findViewById<Button>(R.id.shareButton).setOnClickListener {
            shareInstructions()
        }
    }

    private fun openVideo() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Не удалось открыть видео", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPurchaseLink() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(purchaseUrl))
            startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Не удалось открыть ссылку", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmReplacement() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        android.app.AlertDialog.Builder(this)
            .setTitle("Подтверждение замены")
            .setMessage("Вы подтверждаете, что заменили компонент \"$componentName\" $currentDate?")
            .setPositiveButton("Подтвердить") { _, _ ->
                // Возвращаем результат в FilterDetailsActivity
                val resultIntent = Intent()
                resultIntent.putExtra("replaced_component_id", componentId)
                resultIntent.putExtra("replacement_date", Date().time)
                setResult(RESULT_OK, resultIntent)

                android.widget.Toast.makeText(this, "Замена подтверждена!", android.widget.Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun shareInstructions() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Инструкция по замене: $componentName")
            putExtra(Intent.EXTRA_TEXT,
                "Инструкция по замене $componentName:\n\n$installationInstructions\n\n" +
                        "Видео: $videoUrl\n" +
                        "Купить: $purchaseUrl"
            )
        }

        try {
            startActivity(Intent.createChooser(shareIntent, "Поделиться инструкцией"))
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Не удалось поделиться", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}