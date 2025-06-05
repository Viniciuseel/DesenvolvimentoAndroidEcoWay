plugins {
    alias(libs.plugins.android.application)
<<<<<<< HEAD
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}

android {
    namespace = "com.example.projectecoway"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.projectecoway"
        minSdk = 24
        targetSdk = 35
=======
    alias(libs.plugins.secrets.gradle.plugin) // usar alias do Version Catalog
}

android {
    namespace = "com.example.gmaps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gmaps"
        minSdk = 24
        targetSdk = 34

>>>>>>> fdce6692c0cdcf4bc148b4e32a2ba9d894f6472d
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
<<<<<<< HEAD
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
=======

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
>>>>>>> fdce6692c0cdcf4bc148b4e32a2ba9d894f6472d
    }
}

dependencies {
<<<<<<< HEAD

=======
>>>>>>> fdce6692c0cdcf4bc148b4e32a2ba9d894f6472d
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
<<<<<<< HEAD
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}
=======
    implementation(libs.play.services.maps)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
>>>>>>> fdce6692c0cdcf4bc148b4e32a2ba9d894f6472d
