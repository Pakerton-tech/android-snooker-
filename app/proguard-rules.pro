# Snooker Scorekeeper - ProGuard/R8 Rules
# This is a simple app with no third-party SDKs, minimal rules needed.

# Keep Kotlin serialization/data classes
-keepclassmembers class com.pakertong.snooker.model.** { *; }

# Keep ViewModel state
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Compose-related
-dontwarn androidx.compose.**

# Keep all activities
-keep class * extends android.app.Activity { *; }

# Standard Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Keep app model classes - they're serialized to JSON
-keep class com.pakertong.snooker.model.** {
    *;
}
