package com.aetherkey.ime.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import com.aetherkey.ime.R

/**
 * Thanh Toolbar nằm trên bàn phím:
 * 1. Dịch màn hình (kính lúp)
 * 2. Dịch câu gõ (AI / rocket)
 * 3. Cài đặt (bánh răng)
 */
class ToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val btnScreenTranslate: ImageButton
    private val btnTextTranslate: ImageButton
    private val btnSettings: ImageButton

    var onTranslateScreenClick: (() -> Unit)? = null
    var onTranslateTextClick: (() -> Unit)? = null
    var onSettingsClick: (() -> Unit)? = null

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.toolbar, this, true)

        btnScreenTranslate = findViewById(R.id.btn_translate_screen)
        btnTextTranslate = findViewById(R.id.btn_translate_text)
        btnSettings = findViewById(R.id.btn_settings)

        btnScreenTranslate.setOnClickListener { onTranslateScreenClick?.invoke() }
        btnTextTranslate.setOnClickListener { onTranslateTextClick?.invoke() }
        btnSettings.setOnClickListener { onSettingsClick?.invoke() }
    }
}
