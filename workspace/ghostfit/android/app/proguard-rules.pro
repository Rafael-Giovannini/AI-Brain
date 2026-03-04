# GhostFit ProGuard Rules

# Keep Moshi adapters
-keep class app.ghostfit.data.model.** { *; }
-keepclassmembers class app.ghostfit.data.model.** { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface app.ghostfit.data.remote.** { *; }

# Tink
-keep class com.google.crypto.tink.** { *; }
