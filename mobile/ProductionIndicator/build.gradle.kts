plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.24" apply false
}

// Repositories already defined in settings.gradle.kts via dependencyResolutionManagement
// Additional (e.g., jitpack) can be appended here if needed.
