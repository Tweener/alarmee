import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.jetbrains.compose.compiler)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.nativeCocoaPods)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = ProjectConfiguration.Alarmee.namespace + ".push"
    compileSdk = ProjectConfiguration.Alarmee.compileSDK

    defaultConfig {
        minSdk = ProjectConfiguration.Alarmee.minSDK

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = ProjectConfiguration.Compiler.javaCompatibility
        targetCompatibility = ProjectConfiguration.Compiler.javaCompatibility
    }
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")

        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(ProjectConfiguration.Compiler.jvmTarget))
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "alarmee-push"
            isStatic = true
        }
    }

    cocoapods {
        ios.deploymentTarget = ProjectConfiguration.iOS.deploymentTarget
        noPodspec()
        pod("FirebaseMessaging")
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":alarmee"))

            implementation(libs.kmpkit)
            implementation(compose.foundation)
            implementation(libs.firebase.messaging)
        }
    }
}

// region Publishing

group = ProjectConfiguration.Alarmee.Maven.group
version = ProjectConfiguration.Alarmee.versionName

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    // Only disable signing if the flag is explicitly set to false
    val signAllPublicationsProperty = findProperty("mavenPublishing.signAllPublications")
    if (signAllPublicationsProperty == null || signAllPublicationsProperty.toString().toBoolean()) {
        signAllPublications()
    }

    coordinates(groupId = group.toString(), artifactId = "alarmee-push", version = version.toString())
    configure(
        platform = KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true,
        )
    )

    pom {
        name = ProjectConfiguration.Alarmee.Maven.name
        description = ProjectConfiguration.Alarmee.Maven.description
        url = ProjectConfiguration.Alarmee.Maven.packageUrl

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        issueManagement {
            system = "GitHub Issues"
            url = "${ProjectConfiguration.Alarmee.Maven.packageUrl}/issues"
        }

        developers {
            developer {
                id = ProjectConfiguration.Alarmee.Maven.Developer.id
                name = ProjectConfiguration.Alarmee.Maven.Developer.name
                email = ProjectConfiguration.Alarmee.Maven.Developer.email
            }
        }

        scm {
            connection = "scm:git:git://${ProjectConfiguration.Alarmee.Maven.gitUrl}"
            developerConnection = "scm:git:ssh://${ProjectConfiguration.Alarmee.Maven.gitUrl}"
            url = ProjectConfiguration.Alarmee.Maven.packageUrl
        }
    }
}

// endregion Publishing
