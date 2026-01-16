plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
}

android {
    namespace = "com.quirozsolutions.catalogo1boton"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.quirozsolutions.catalogo1boton"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE.md",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    /* ---------------- COMPOSE ---------------- */
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    /* ---------------- DATA ---------------- */
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil (imagenes)
    implementation("io.coil-kt:coil-compose:2.7.0")

    /* ---------------- ROOM ---------------- */
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    /* ---------------- WORK ---------------- */
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    /* ---------------- JSON ---------------- */
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    /* ---------------- GOOGLE AUTH ---------------- */
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    /* ---------------- GOOGLE DRIVE (ESTABLE) ---------------- */
    implementation("com.google.api-client:google-api-client-android:1.35.0")
    implementation("com.google.http-client:google-http-client-android:1.43.3")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")
    implementation("com.google.apis:google-api-services-drive:v3-rev20230815-2.0.0")
    testImplementation("junit:junit:4.13.2")

}
