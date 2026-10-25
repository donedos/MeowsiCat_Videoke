# Add project specific R8 rules here.
# For more details, see http://developer.android.com/guide/developing/tools/proguard.html

# --- JNI & Native Libraries ---
-keep class com.songhub.android.database.NativeLib {
    *;
}

-keep class com.un4seen.bass.** {
    *;
}

-keepclassmembers class * {
    native <methods>;
}

# --- Database & Data Models ---
-keep class com.meowsicat.android.database.** { *; }
-keep class com.meowsicat.android.model.** { *; }
-keep class com.outofthebox.meowsicattv.data.** { *; }

# --- Keep Jetpack Compose & UI components ---
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
