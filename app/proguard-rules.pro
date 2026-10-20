# kotlinx.serialization: keep generated serializers for @Serializable classes
# (the library ships its own consumer rules; these cover the app's models).
-keepattributes *Annotation*, InnerClasses
-keepclassmembers @kotlinx.serialization.Serializable class suft.android.ayahiq.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# Type-safe Navigation routes are looked up through their serializers.
-keep @kotlinx.serialization.Serializable class suft.android.ayahiq.ui.*Route { *; }
-keep class suft.android.ayahiq.ui.*Route$* { *; }
