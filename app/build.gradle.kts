plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.dotmail.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.dotmail.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "1.1"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    val debugKeystorePath = System.getenv("DOTMAIL_DEBUG_KEYSTORE_PATH")
    val debugKeystorePassword = System.getenv("DOTMAIL_DEBUG_KEYSTORE_PASSWORD")
    val debugKeyAlias = System.getenv("DOTMAIL_DEBUG_KEY_ALIAS")
    val debugKeyPassword = System.getenv("DOTMAIL_DEBUG_KEY_PASSWORD")

    if (!debugKeystorePath.isNullOrBlank() && !debugKeystorePassword.isNullOrBlank() && !debugKeyAlias.isNullOrBlank() && !debugKeyPassword.isNullOrBlank()) {
        signingConfigs.getByName("debug").apply {
            storeFile = file(debugKeystorePath)
            storePassword = debugKeystorePassword
            keyAlias = debugKeyAlias
            keyPassword = debugKeyPassword
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.08.00")
    implementation(composeBom)

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.10.1")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("com.google.android.gms:play-services-auth:22.0.0")
}
