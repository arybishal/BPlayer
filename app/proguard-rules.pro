# Pulse Android Music Player ProGuard / R8 Rules

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.TypeConverter
-dontwarn androidx.room.paging.**

# AndroidX Media3 (ExoPlayer & MediaSession)
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keepattributes *Annotation*

# JAudioTagger (uses reflection for ID3 and tag headers)
-keep class org.jaudiotagger.** { *; }
-dontwarn org.jaudiotagger.**

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
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Pulse / BPlayer Domain & State Models
-keep class com.pulse.player.domain.model.** { *; }
-keep class com.pulse.player.service.model.** { *; }
-keep class com.pulse.player.data.remote.** { *; }
-keep class com.pulse.player.data.local.entity.** { *; }
-keep class com.pulse.player.data.autoeq.** { *; }
-keep class com.pulse.player.data.scrobbler.** { *; }
-keep class com.pulse.player.domain.model.backup.** { *; }
-keep class com.lastwave.** { *; }
-keep class com.decent.usbaudio.** { *; }

# JNI & Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
