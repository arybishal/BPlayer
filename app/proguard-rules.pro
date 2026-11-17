# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit & OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-keepattributes Signature
-keepattributes Exceptions
