# AetherKey

**AI-powered Android Keyboard (IME)**  
BYOK Translate • Privacy-first • Local Dictionary • Community Sync (Hugging Face)

## Features (v0.1.0 skeleton)

- Core InputMethodService + Toolbar (Screen Translate / AI Translate / Settings)
- OpenAI-compatible AI client with **Key Rotation & Failover**
- EncryptedSharedPreferences + Android Keystore for API keys
- Privacy Guard (password fields, no-learning flag, regex filter)
- Room local dictionary (frequency-based)
- Material 3 vibrant theme (AMOLED ready)
- GitHub Actions auto-build APK

## Next steps

1. Full QWERTY + Vietnamese Telex/VNI (fork OpenBoard logic)
2. Real Accessibility / MediaProjection + ML Kit OCR
3. Theme engine (light/dark/AMOLED + custom wallpaper + opacity)
4. Hugging Face dictionary sync (read-only public)
5. Export/Import dictionary JSON

## Build

```bash
# In Android Studio or with Gradle wrapper
./gradlew assembleDebug
```

APK will be in `app/build/outputs/apk/debug/`

## License

Based on AOSP / OpenBoard patterns → GPL-3.0 compatible.
Do not embed any write tokens.

## Author

AetherKey team – privacy first.
