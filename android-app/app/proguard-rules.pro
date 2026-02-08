# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep BuildConfig
-keep class com.prembhaskal.expensetracker.BuildConfig { *; }

# Gson: keep API DTOs and model classes used by reflection
-keep class com.prembhaskal.expensetracker.data.remote.ApiDto$* { *; }
-keep class com.prembhaskal.expensetracker.data.remote.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.Unsafe

# Room and entities (Room keeps these by default; keep for safety)
-keep class com.prembhaskal.expensetracker.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile