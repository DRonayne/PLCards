# Keep Hilt-generated classes
-keep class dagger.hilt.internal.aggregatedroot.codegen.*
-keep class com.darach.plcards.Hilt_PLCardsApp

# Keep Moshi-generated adapters
-keep class com.squareup.moshi.JsonAdapter
-keep class com.darach.plcards.data.remote.dto.** { *; }
-keep @com.squareup.moshi.Json class *
-keep class kotlin.reflect.jvm.internal.**

# Keep Coil classes
-keep class coil.** { *; }

# General rules for Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.Metadata