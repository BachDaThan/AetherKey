package com.aetherkey.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import com.aetherkey.ime.R
import com.aetherkey.ime.ai.AiTranslateClient
import com.aetherkey.ime.dictionary.LocalDictionaryDb
import com.aetherkey.ime.keyboard.ToolbarView
import com.aetherkey.ime.security.PrivacyGuard
import com.aetherkey.ime.security.SecureKeyStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Core InputMethodService for AetherKey.
 * Based on AOSP/OpenBoard patterns + AI Translate toolbar.
 */
class AetherInputMethodService : InputMethodService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var rootView: View
    private lateinit var toolbar: ToolbarView
    private lateinit var aiClient: AiTranslateClient
    private lateinit var privacyGuard: PrivacyGuard
    private lateinit var secureStorage: SecureKeyStorage
    private lateinit var dictDb: LocalDictionaryDb

    private var currentIc: InputConnection? = null
    private var isPasswordOrSensitive = false

    override fun onCreate() {
        super.onCreate()
        secureStorage = SecureKeyStorage(this)
        aiClient = AiTranslateClient(secureStorage)
        privacyGuard = PrivacyGuard()
        dictDb = LocalDictionaryDb.getInstance(this)
    }

    override fun onCreateInputView(): View {
        rootView = layoutInflater.inflate(R.layout.input_view, null)
        toolbar = rootView.findViewById(R.id.toolbar)
        setupToolbar()
        // TODO: attach full keyboard layout (QWERTY + Telex) here
        // For now skeleton shows toolbar + placeholder keys
        return rootView
    }

    private fun setupToolbar() {
        toolbar.setOnTranslateScreenClick {
            Toast.makeText(this, "Screen Translate (Accessibility) - coming next", Toast.LENGTH_SHORT).show()
            // startScreenTranslate()
        }

        toolbar.setOnTranslateTextClick {
            translateCurrentComposingText()
        }

        toolbar.setOnSettingsClick {
            val intent = android.content.Intent(this, com.aetherkey.ime.ui.settings.SettingsActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun translateCurrentComposingText() {
        val ic = currentIc ?: return
        val extracted = ic.getExtractedText(android.view.inputmethod.ExtractedTextRequest(), 0)
        val text = extracted?.text?.toString()?.trim()
        if (text.isNullOrBlank()) {
            Toast.makeText(this, "Không có văn bản để dịch", Toast.LENGTH_SHORT).show()
            return
        }

        if (privacyGuard.shouldBlockSending(text) || isPasswordOrSensitive) {
            Toast.makeText(this, "Bảo mật: không gửi nội dung nhạy cảm", Toast.LENGTH_SHORT).show()
            return
        }

        serviceScope.launch {
            try {
                val translated = withContext(Dispatchers.IO) {
                    aiClient.translate(
                        text = text,
                        targetLang = secureStorage.getTargetLanguage() ?: "en"
                    )
                }
                if (translated != null) {
                    // Replace the whole extracted text
                    ic.beginBatchEdit()
                    ic.deleteSurroundingText(text.length, 0)
                    ic.commitText(translated, 1)
                    ic.endBatchEdit()
                } else {
                    Toast.makeText(this@AetherInputMethodService, "Dịch thất bại (kiểm tra API Key)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AetherInputMethodService, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentIc = currentInputConnection
        isPasswordOrSensitive = privacyGuard.isSensitiveField(attribute)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentIc = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // clean up if needed
    }
}
