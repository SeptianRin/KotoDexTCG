##############################################
## Retrofit + Gson
##############################################
#-dontwarn retrofit2.**
#-keep class retrofit2.** { *; }
#-keepattributes Signature, Exceptions
#-keep class com.google.gson.** { *; }
#-keepclassmembers class * {
#    @com.google.gson.annotations.SerializedName <fields>;
#}
#-keepattributes *Annotation*
#
##############################################
## Room Database and DAOs
##############################################
#-keep class androidx.room.RoomDatabase { *; }
#-keep class * extends androidx.room.RoomDatabase { *; }
#-keep @androidx.room.Database class * { *; }
#-keep @androidx.room.Entity class * { *; }
#-keep @androidx.room.Dao class * { *; }
#-keep @androidx.room.TypeConverter class * { *; }
#-dontwarn androidx.room.paging.**
#
##############################################
## Kotlin + Coroutines
##############################################
#-keep class kotlin.Metadata { *; }
#-keepclassmembers class **$WhenMappings { <fields>; }
#-dontwarn kotlinx.coroutines.**
#
##############################################
## ViewModel + Lifecycle
##############################################
#-keep class androidx.lifecycle.** { *; }
#-dontwarn androidx.lifecycle.**
#
##############################################
## OkHttp (used by Retrofit)
##############################################
#-dontwarn okhttp3.**
#-keep class okhttp3.** { *; }
#
##############################################
## Jetpack Compose (safe fallback)
##############################################
#-dontwarn androidx.compose.**
#-keep class androidx.compose.** { *; }
#
##############################################
## Application model package (replace as needed)
##############################################
#-keep class io.github.septianrin.kotodextcg.data.** { *; }  # Adjust if your model package is different
#
## Keep Retrofit generic type information
#-keepattributes Signature
#-keepattributes *Annotation*
#
## Keep generic types for Gson/Retrofit
#-keep class com.yourpackage.model.** { *; }  # Adjust to your actual model package
#-keepclassmembers class * {
#    @com.google.gson.annotations.SerializedName <fields>;
#}
#
## Keep Retrofit converters
#-keep class retrofit2.** { *; }
#-keep interface retrofit2.** { *; }
#-dontwarn retrofit2.**
