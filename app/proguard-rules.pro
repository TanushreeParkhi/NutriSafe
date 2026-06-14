# Keep reflection-backed API DTOs stable for Retrofit/Gson.
-keep class com.priveat.app.data.remote.** { *; }
-keep class com.priveat.app.data.model.** { *; }
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations

# Retrofit uses dynamic proxies.
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
