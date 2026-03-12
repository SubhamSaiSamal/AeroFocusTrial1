# Add project specific ProGuard rules here.
# Keep Room entities
-keep class com.aerofocus.app.data.db.entity.** { *; }

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
