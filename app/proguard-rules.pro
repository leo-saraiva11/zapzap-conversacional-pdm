# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Supabase
-keep class io.github.jan.supabase.** { *; }
