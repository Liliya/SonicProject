import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val moduleName = "sonic_ui"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)

    alias(libs.plugins.serialization)

    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}


kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = moduleName
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(projects.sonicState)
            implementation(projects.sonicHelpers)

            // serialization
            implementation(libs.kotlinx.serialization.json)

            // compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // decompose
            implementation(libs.decompose)
            implementation(libs.decompose.compose)

            // decompose-essenty
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.stateKeeper)
            implementation(libs.essenty.backHandler)

            implementation(libs.kmp.firebase.database)
            implementation(libs.kmp.firebase.firestore)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)
            implementation(libs.okio)

            // images
            implementation(libs.landscapist.coil3)
        }
    }
}

android {
    namespace = "com.ato.$moduleName"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
}
