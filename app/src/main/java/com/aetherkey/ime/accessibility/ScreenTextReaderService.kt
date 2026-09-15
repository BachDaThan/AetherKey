package com.aetherkey.ime.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Optional AccessibilityService for Screen Translate.
 * Chỉ chạy khi user bấm nút, không tự động 24/7.
 * Cần user bật trong Settings > Accessibility.
 */
class ScreenTextReaderService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // TODO: extract text from window when triggered by toolbar button
        // For now just skeleton
    }

    override fun onInterrupt() {
        // cleanup
    }
}
