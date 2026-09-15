package com.aetherkey.ime.ui.settings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aetherkey.ime.R
import com.aetherkey.ime.security.SecureKeyStorage

class SettingsActivity : AppCompatActivity() {

    private lateinit var storage: SecureKeyStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        storage = SecureKeyStorage(this)

        val etKeys = findViewById<EditText>(R.id.et_api_keys)
        val etBaseUrl = findViewById<EditText>(R.id.et_base_url)
        val etModel = findViewById<EditText>(R.id.et_model)
        val etTarget = findViewById<EditText>(R.id.et_target_lang)
        val btnSave = findViewById<Button>(R.id.btn_save)

        // Load current
        etKeys.setText(storage.getApiKeys().joinToString("\n"))
        etBaseUrl.setText(storage.getBaseUrl() ?: "https://api.openai.com/v1")
        etModel.setText(storage.getModelName() ?: "gpt-4o-mini")
        etTarget.setText(storage.getTargetLanguage() ?: "en")

        btnSave.setOnClickListener {
            val keys = etKeys.text.toString().split("\n").map { it.trim() }.filter { it.isNotBlank() }
            storage.setApiKeys(keys)
            storage.setBaseUrl(etBaseUrl.text.toString().trim())
            storage.setModelName(etModel.text.toString().trim())
            storage.setTargetLanguage(etTarget.text.toString().trim())
            Toast.makeText(this, "Đã lưu cấu hình an toàn", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
