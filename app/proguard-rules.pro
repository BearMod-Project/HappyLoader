# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.itsarisu.erroldec.** { *; }
-keep class com.fvbox.lib.** { *; }
-keep class com.fvbox.mirror.** { *; }
# -keep class  net_62v.external.** { *; }  # Commented out - package not available
-keep class com.happy.pro.libhelper.MetaStubs.** { *; }

-optimizations

-keepattributes Signature

-keep class com.happy.pro.floating.FloatService {
    public <methods>;
}

-keep class com.itsarisu.erroldec.service.** {*;}
-keep public class com.happy.pro.floating.FloatService


-keep class io.grpc.** {*;}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontwarn javax.annotation.Nullable
-dontwarn com.squareup.okhttp.CipherSuite
-dontwarn com.squareup.okhttp.ConnectionSpec
-dontwarn com.squareup.okhttp.TlsVersion
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn javax.lang.model.element.Modifier
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder

-dontwarn com.hjq.permissions.**
-dontwarn top.niunaijun.android_mirror.**
-dontwarn top.canyie.pine.**
-dontwarn top.canyie.pine.xposed.**
-dontwarn top.canyie.dreamland.**
-dontwarn top.niunaijun.blackbox.**

-keep class com.hjq.permissions.** {
    public <methods>;
}

-keep class top.niunaijun.android_mirror.** {
    public <methods>;
}

-keep class top.canyie.pine.** {
    public <methods>;
}

-keep class top.canyie.pine.xposed.** {
    public <methods>;
}

-keep class top.canyie.dreamland.** {
    public <methods>;
}

-keep class top.niunaijun.blackbox.** {
    public <methods>;
}

-keep class ** {
    public <methods>;
}

-keep class * {
    public <methods>;
}

-keep class com.hjq.permissions.** {*;}
-keep class top.niunaijun.android_mirror.** {*;}
-keep class top.canyie.pine.** {*;}
-keep class top.canyie.pine.xposed.** {*;}
-keep class top.canyie.dreamland.** {*;}
-keep class top.niunaijun.blackbox.** {*;}

# ==========================================
# BEAR-LOADER Download Manager Architecture
# ==========================================

# Keep download manager interfaces
-keep interface com.happy.pro.download.interfaces.** { *; }

# Keep download manager implementations
-keep class com.happy.pro.download.impl.** {
    public <methods>;
    public <fields>;
}

# Keep download callback methods (critical for functionality)
-keepclassmembers class * implements com.happy.pro.download.interfaces.IDownloadManager$DownloadCallback {
    public void on*(java.lang.String, **);
    public void on*(**);
}

# Keep download request and progress classes
-keep class com.happy.pro.download.interfaces.IDownloadManager$DownloadRequest {
    public <fields>;
    public <methods>;
}

-keep class com.happy.pro.download.interfaces.IDownloadManager$DownloadProgress {
    public <fields>;
    public <methods>;
}

-keep class com.happy.pro.download.interfaces.IDownloadManager$DownloadType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep FileMapping configuration (critical for KeyAuth file IDs)
-keep class com.happy.pro.download.config.FileMapping {
    public static <methods>;
    public static <fields>;
}

-keep class com.happy.pro.download.config.FileMapping$GameInfo {
    public <fields>;
    public <methods>;
}

# Keep download utilities
-keep class com.happy.pro.download.utils.** {
    public static <methods>;
}

# Keep KeyAuth API implementation
-keep class com.happy.pro.download.impl.KeyAuthAPIImpl {
    public <methods>;
}

# Keep download tasks
-keep class com.happy.pro.download.tasks.** {
    public <methods>;
}

# ==========================================
# Dependencies for Download Manager
# ==========================================

# Gson (for KeyAuth API JSON responses)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp (for KeyAuth API calls)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Work Manager (for background downloads)
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Commons IO (for file operations)
-dontwarn org.apache.commons.io.**
-keep class org.apache.commons.io.** { *; }

# ==========================================
# KeyAuth API Response Classes (if needed)
# ==========================================

# If you create specific classes for KeyAuth responses, add them here:
# -keep class com.happy.pro.download.models.** { *; }

