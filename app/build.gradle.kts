import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing: `mise run keystore` creates keystore.properties; CI can set HAFIZ_KEYSTORE_FILE,
// HAFIZ_KEYSTORE_PASSWORD, HAFIZ_KEY_ALIAS and HAFIZ_KEY_PASSWORD instead.
val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

fun signingProp(key: String, env: String): String? =
    keystoreProps.getProperty(key) ?: providers.environmentVariable(env).orNull

val releaseStoreFile: File? = signingProp("storeFile", "HAFIZ_KEYSTORE_FILE")?.let { rootProject.file(it) }
val hasReleaseKeystore = releaseStoreFile?.exists() == true

// Opt-in for builds that are only run locally or in CI (-PallowDebugSigning or HAFIZ_ALLOW_DEBUG_SIGNING=1).
val allowDebugSigning: Boolean = providers.gradleProperty("allowDebugSigning")
    .orElse(providers.environmentVariable("HAFIZ_ALLOW_DEBUG_SIGNING"))
    .map { it == "true" || it == "1" }
    .getOrElse(false)

val checkReleaseSigning = tasks.register("checkReleaseSigning") {
    val configured = hasReleaseKeystore || allowDebugSigning
    val missingFile = releaseStoreFile?.takeIf { !it.exists() }?.path
    doLast {
        if (!configured) {
            throw GradleException(
                (if (missingFile != null) "The release keystore $missingFile does not exist. " else "") +
                    "Release signing is not configured: run `mise run keystore`, set the HAFIZ_KEYSTORE_* " +
                    "variables, or set HAFIZ_ALLOW_DEBUG_SIGNING=1 to sign with the public debug key " +
                    "(never distribute such builds).",
            )
        }
    }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(checkReleaseSigning) }

android {
    namespace = "suft.android.hafiz"
    compileSdk = 37

    defaultConfig {
        // Package changed from the original zozo.android.ayahsurra; installs as a separate app.
        applicationId = "suft.android.hafiz"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (hasReleaseKeystore) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = signingProp("storePassword", "HAFIZ_KEYSTORE_PASSWORD")
                keyAlias = signingProp("keyAlias", "HAFIZ_KEY_ALIAS")
                keyPassword = signingProp("keyPassword", "HAFIZ_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Never silently fall back to the public debug key: such a build could be sideloaded as "the" release
            // and a leaked debug-signed update path is a supply-chain hazard. `checkReleaseSigning` explains how to opt in.
            signingConfig = when {
                hasReleaseKeystore -> signingConfigs.getByName("release")
                allowDebugSigning -> signingConfigs.getByName("debug")
                else -> null
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
            all {
                it.systemProperty("java.awt.headless", "true")
                // Robolectric 4.17 (SDK 36) reaches into JDK internals.
                it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.database)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.espresso.core)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
}
