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

# Keep Hilt generated 
-keep class dagger.hilt.** { *; }

# Obfuscate IntegrityGuard aggressively
-keepclassmembers class com.onserver1.app.util.IntegrityGuard {
    public static java.lang.String computeToken();
    public static java.lang.String resolveCreditEn();
    public static java.lang.String resolveCreditAr();
    public static java.lang.String resolveDomain();
    public static java.lang.String resolveUrl();
    public static boolean verify();
}
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Optimize and obfuscate
-optimizationpasses 5
-repackageclasses ''
-allowaccessmodification
