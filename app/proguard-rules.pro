
-keep class com.nhn.core.network.** { *; }

-keep class com.nhn.core.domain.model.** { *; }
-keep class com.nhn.core.data.datasource.remote.dto.** { *; }

-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

-keep class **.*ViewModel { *; }
-keep class **.*ViewModel$* { *; }

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

-dontwarn androidx.compose.**
-keep class kotlin.Metadata { *; }