# These are the rules for your app-specific ProGuard configuration.

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Coroutines internal classes
-keepclassmembers class kotlinx.coroutines.internal.** {
    *;
}

# Keep your data/model classes that are used by Retrofit/Gson for serialization.
# This is one of the most important rules. It tells R8 not to remove or
# obfuscate the classes in your 'data.model' package.
-keep class io.github.septianrin.kotodextcg.data.model.** { *; }
-keep class io.github.septianrin.kotodextcg.data.network.** { *; }


# Keep classes needed by the Coil image loading library
-keep class coil.** { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# General rules for keeping classes that might be used via reflection
-keepattributes Signature
-keepattributes *Annotation*

# Keep names of any class that is an interface.
-keepnames interface ** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson model classes (replace `your.package.model.**` with your actual package)
-keep class your.package.model.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*

# Keep class names and fields with annotations like @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Room database and DAO classes
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep annotated Entities, DAOs, Converters, etc.
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.TypeConverter class * { *; }

# Keep Kotlin metadata (needed for reflection-based libraries like Room)
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod