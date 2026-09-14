plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.emailaliassandbox"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.emailaliassandbox"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
}
