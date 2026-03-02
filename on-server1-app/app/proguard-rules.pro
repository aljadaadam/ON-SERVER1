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
