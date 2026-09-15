package com.aetherkey.ime.security

import android.text.InputType
import android.view.inputmethod.EditorInfo
import java.util.regex.Pattern

/**
 * Privacy Guard tuyệt đối:
 * - Tắt học từ khi password / no personalized learning
 * - Chặn gửi số điện thoại, email, CCCD, STK, URL, @...
 */
class PrivacyGuard {

    private val sensitivePatterns = listOf(
        Pattern.compile("""\b\d{9,12}\b"""),                     // phone / CCCD approx
        Pattern.compile("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}"""), // email
        Pattern.compile("""https?://\S+"""),                     // URL
        Pattern.compile("""\b\d{10,20}\b"""),                    // account numbers
        Pattern.compile("""@\w+""")                             // mentions
    )

    fun isSensitiveField(info: EditorInfo?): Boolean {
        if (info == null) return false
        val type = info.inputType
        val variation = type and InputType.TYPE_MASK_VARIATION

        // Password fields
        if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
            variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
            return true
        }

        // IME_FLAG_NO_PERSONALIZED_LEARNING
        if ((info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0) {
            return true
        }

        return false
    }

    fun shouldBlockSending(text: String): Boolean {
        for (p in sensitivePatterns) {
            if (p.matcher(text).find()) return true
        }
        return false
    }

    fun shouldBlockLearning(text: String, info: EditorInfo?): Boolean {
        return isSensitiveField(info) || shouldBlockSending(text)
    }
}
