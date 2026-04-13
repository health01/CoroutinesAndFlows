# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK/tools/proguard/proguard-android.txt

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson / JSON DTOs
-keep class com.example.coroutinesflows.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
