plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.google.services)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget()

    // region iOS configuration

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "composeApp"
            isStatic = true

            // Add here any extra framework dependencies
            export(project(":alarmee"))
        }
    }

    // endregion iOS configuration

    sourceSets {
        commonMain.dependencies {
            api(project(":alarmee-push"))

            implementation(libs.kmpkit)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(libs.compose.multiplatform.material3)
        }

        androidMain.dependencies {
            implementation(libs.android.activity)
            implementation(libs.android.activity.compose)
        }
    }
}

android {
    namespace = ProjectConfiguration.Alarmee.packageName + ".sample"
    compileSdk = ProjectConfiguration.Alarmee.compileSDK

    defaultConfig {
        applicationId = ProjectConfiguration.Alarmee.packageName + ".sample"
        minSdk = ProjectConfiguration.Alarmee.minSDK
        targetSdk = ProjectConfiguration.Alarmee.compileSDK
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility
    }

    buildFeatures {
        compose = true
    }
}
