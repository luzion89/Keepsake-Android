# Keepsake Android ProGuard Rules

# Keep Room entities
-keep class com.keepsake.app.data.local.entity.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.keepsake.app.**$$serializer { *; }
-keepclassmembers class com.keepsake.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.keepsake.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
