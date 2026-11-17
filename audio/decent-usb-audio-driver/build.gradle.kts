plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.decent.usbaudio"
    compileSdk = 37

    defaultConfig {
        minSdk = (project.findProperty("minSdk") as? String)?.toIntOrNull() ?: 26
        (project.findProperty("targetAbis") as? String)?.let { prop ->
            ndk {
                abiFilters.addAll(prop.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
        }
        externalNativeBuild { cmake { cppFlags("-fno-fast-math") } }
    }

    buildTypes {
        getByName("debug")
        getByName("release")
        create("rawRelease") {
            initWith(getByName("release"))
            isMinifyEnabled = false
        }
    }

    externalNativeBuild {
        cmake { path("src/main/jni/CMakeLists.txt") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
}
