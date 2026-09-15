package com.aetherkey.ime.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Lưu API Keys an toàn bằng EncryptedSharedPreferences + Android Keystore.
 * Không bao giờ hardcode token.
 */
class SecureKeyStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "aether_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKeys(): List<String> {
        val raw = prefs.getString("api_keys", "") ?: ""
        return raw.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun setApiKeys(keys: List<String>) {
        prefs.edit().putString("api_keys", keys.joinToString("\n")).apply()
    }

    fun getBaseUrl(): String? = prefs.getString("base_url", null)
    fun setBaseUrl(url: String) = prefs.edit().putString("base_url", url).apply()

    fun getModelName(): String? = prefs.getString("model_name", null)
    fun setModelName(model: String) = prefs.edit().putString("model_name", model).apply()

    fun getTargetLanguage(): String? = prefs.getString("target_lang", "en")
    fun setTargetLanguage(lang: String) = prefs.edit().putString("target_lang", lang).apply()
}
