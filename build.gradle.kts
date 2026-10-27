import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.android.kotlin.multiplatform.library).apply(false)
    alias(libs.plugins.jetbrains.compose).apply(false)
    alias(libs.plugins.jetbrains.compose.compiler).apply(false)
    alias(libs.plugins.kotlin.nativeCocoaPods).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.dependency.versions) // ./gradlew dependencyUpdates
    alias(libs.plugins.google.services).apply(false)
}

// Mocha 11 pulls vulnerable serialize-javascript and diff versions into kotlin-js-store/yarn.lock
plugins.withType<NodeJsRootPlugin> {
    the<NodeJsRootExtension>().versions.mocha.version = "12.0.3"
}
