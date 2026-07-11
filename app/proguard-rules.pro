# ══════════════════════════════════════════════════════════════
# LocalShare ProGuard / R8 Rules — Release Build Fix
# ══════════════════════════════════════════════════════════════

# ─── Keep the ENTIRE app package ─────────────────────────────
-keep class com.localshare.app.** { *; }
-keepclassmembers class com.localshare.app.** { *; }

# ─── NanoHTTPD (HTTP Server) ─────────────────────────────────
-keep class fi.iki.elonen.** { *; }
-keepnames class fi.iki.elonen.** { *; }
-keep class org.nanohttpd.** { *; }
-dontwarn fi.iki.elonen.**
-dontwarn org.nanohttpd.**

# ─── ZXing (QR Code) ────────────────────────────────────────
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.** { *; }
-dontwarn com.journeyapps.**
-dontwarn com.google.zxing.**

# ─── Material Color Utilities ────────────────────────────────
-keep class com.google.android.material.color.** { *; }
-keep class com.google.android.material.color.utilities.** { *; }
-dontwarn com.google.android.material.color.utilities.**

# ─── Kotlinx Coroutines ─────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ─── Room Database ───────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep class * {
    @androidx.room.Insert <methods>;
    @androidx.room.Query <methods>;
    @androidx.room.Update <methods>;
    @androidx.room.Delete <methods>;
}
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# ─── DataStore ───────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }
-dontwarn androidx.datastore.**

# ─── Coil (Image Loading) ───────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ─── Google Fonts (Variable Font loading) ────────────────────
-keep class androidx.compose.ui.text.googlefonts.** { *; }

# ─── AndroidX WorkManager ───────────────────────────────────
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# ─── AndroidX Media3 / ExoPlayer ─────────────────────────────
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ─── AndroidX Core & Lifecycle ──────────────────────────────
-keep class androidx.core.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.core.**
-dontwarn androidx.lifecycle.**

# ─── AndroidX Activity & Navigation ─────────────────────────
-keep class androidx.activity.** { *; }
-keep class androidx.navigation.** { *; }
-dontwarn androidx.activity.**
-dontwarn androidx.navigation.**

# ─── All Enums ───────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepnames enum *

# ─── Kotlin Metadata (needed for reflection/serialization) ──
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# ─── Prevent stripping of Parcelable implementations ────────
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ─── Prevent stripping of Serializable implementations ──────
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ─── org.json (JSON parsing used in server) ──────────────────
-keep class org.json.** { *; }
-dontwarn org.json.**

# ─── Java Net / IO (used by NanoHTTPD) ──────────────────────
-keep class java.net.** { *; }
-keep class java.io.** { *; }
-keep class java.util.zip.** { *; }
-dontwarn java.net.**
-dontwarn java.io.**

# ─── Android Widget ──────────────────────────────────────────
-keep class * extends android.appwidget.AppWidgetProvider
-keep class * extends android.appwidget.AppWidgetHost

# ─── Prevent R8 from stripping Compose internals ────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ─── Critical: Prevent thread-related optimization issues ────
# NanoHTTPD uses threads; R8 optimization can cause deadlocks
-optimizations !code/simplification/variable
-optimizations !code/simplification/arithmetic

# ─── Keep all annotations (some are used at runtime) ────────
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
