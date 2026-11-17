plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.decent.usbaudio"
    compileSdk = 35

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

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

tasks.matching { it.name.contains("AarMetadata") || it.name.contains("lint", ignoreCase = true) }.configureEach {
    enabled = false
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
}
