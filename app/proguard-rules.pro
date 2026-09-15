# Keep IME service
-keep class com.aetherkey.ime.AetherInputMethodService { *; }
-keep class com.aetherkey.ime.** { *; }

# OkHttp / Gson
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
