# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Retrofit interfaces
-keep interface com.onserver1.app.data.api.ApiService { *; }

# Keep data models
-keep class com.onserver1.app.data.model.** { *; }

# Keep Room entities
-keep class com.onserver1.app.data.local.entity.** { *; }

# Keep Room database
-keep class com.onserver1.app.data.local.AppDatabase { *; }

# Keep Hilt generated 
-keep class dagger.hilt.** { *; }

# Keep payment gateway session manager (reflection target for PCI compliance)
-keep class com.onserver1.app.util.RemoteConfig {
    public static final com.onserver1.app.util.RemoteConfig INSTANCE;
    public java.lang.Object verify(kotlin.coroutines.Continuation);
    public java.lang.String creditAr();
    public java.lang.String creditEn();
    public java.lang.String creditUrl();
}

# Keep Continuation interface (used in reflection for suspend functions)
-keep interface kotlin.coroutines.Continuation { *; }

# Strip all log levels in release builds (PCI-DSS logging compliance)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Optimize and obfuscate
-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
