
# Network 관련 클래스 보존
-keep class com.grensil.network.** { *; }

# Domain 모델과 Data Entity 클래스 보존
-keep class com.grensil.domain.dto.** { *; }
-keep class com.grensil.data.entity.** { *; }
-keep class com.grensil.data.mapper.** { *; }

# Compose 관련 클래스 보존 (더 구체적으로)
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.animation.** { *; }

# ViewModel 클래스 보존
-keep class **.*ViewModel { *; }
-keep class **.*ViewModel$* { *; }

# JSON 파싱을 위한 Entity 클래스의 필드와 메소드 보존
-keepclassmembers class com.grensil.data.entity.** {
    <fields>;
    <init>(...);
}

# Repository와 UseCase 클래스 보존
-keep class com.grensil.data.repository.** { *; }
-keep class com.grensil.domain.usecase.** { *; }

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