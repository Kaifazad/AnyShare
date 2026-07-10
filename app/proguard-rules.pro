# Add project specific ProGuard rules here.
-keep class fi.iki.elonen.** { *; }
-keep class com.google.zxing.** { *; }

# NanoHTTPD
-keep class fi.iki.elonen.** { *; }
-keepnames class fi.iki.elonen.** { *; }

# Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class * {
    @androidx.room.Insert <methods>;
    @androidx.room.Query <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
}

# General
-dontwarn fi.iki.elonen.**
-dontwarn org.nanohttpd.**

# Keep data models and enums to prevent serialization crashes (Room, DataStore, JSON)
-keep class com.localshare.app.data.** { *; }

# Keep all enums safe
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames enum *
