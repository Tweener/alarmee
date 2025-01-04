import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.jetbrains.compose.compiler).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.nexus.sonatype)
    alias(libs.plugins.dependency.versions) // ./gradlew dependencyUpdates
    alias(libs.plugins.kotlin.serialization).apply(false)
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(gradleLocalProperties(rootDir, providers).getProperty("sonatype.username") ?: System.getenv("OSSRH_USERNAME"))
            password.set(gradleLocalProperties(rootDir, providers).getProperty("sonatype.password") ?: System.getenv("OSSRH_PASSWORD"))
            stagingProfileId.set(gradleLocalProperties(rootDir, providers).getProperty("sonatype.stagingProfileId") ?: System.getenv("OSSRH_STAGING_PROFILE_ID"))
        }
    }
}
