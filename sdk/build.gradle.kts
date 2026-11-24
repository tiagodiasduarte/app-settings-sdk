import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    namespace = "pt.tiagodiasduarte.appsettings.sdk"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }
    resourcePrefix = "appsettings_"

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            // Robolectric needs the merged resources and manifest.
            isIncludeAndroidResources = true
        }
        // Robolectric on API 36 needs JDK internals that JDK 17+ hides by default.
        unitTests.all { it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED") }
    }
}

kotlin {
    explicitApi()
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
}

room {
    schemaDirectory("$projectDir/schemas")
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("androidx.compose.ui.tooling.preview.Preview")
                annotatedBy("androidx.compose.runtime.Composable")

                classes(
                    // Generated code
                    "*ComposableSingletons*",
                    "*_Impl",
                    "*_Impl$*",
                    "pt.tiagodiasduarte.appsettings.sdk.BuildConfig",

                    // Activity, DI and theme
                    "pt.tiagodiasduarte.appsettings.sdk.presentation.AppSettingsActivity*",
                    "pt.tiagodiasduarte.appsettings.sdk.di.*",
                    "pt.tiagodiasduarte.appsettings.sdk.presentation.theme.*",
                )
            }
        }

        verify {
            rule("line-coverage") {
                minBound(
                    minValue = 90,
                    coverageUnits = CoverageUnit.LINE,
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                )
            }
            rule("branch-coverage") {
                minBound(
                    minValue = 90,
                    coverageUnits = CoverageUnit.BRANCH,
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                )
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)

    // Host apps bring their own OkHttp through Retrofit; the SDK never picks its version.
    compileOnly(libs.okhttp)

    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
