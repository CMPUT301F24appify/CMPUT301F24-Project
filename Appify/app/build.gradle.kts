plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {

    namespace = "com.example.appify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.appify"
        minSdk = 24
        targetSdk = 34
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<Test> {
        useJUnitPlatform() // Make all tests use JUnit 5
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-storage:20.0.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    testImplementation("org.robolectric:robolectric:4.6.1")
    testImplementation("org.mockito:mockito-core:4.0.0")
    testImplementation("org.mockito:mockito-inline:4.0.0")
    testImplementation("org.mockito:mockito-inline:4.0.0")
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.core)
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.0.1")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.0.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    androidTestImplementation ("androidx.test.ext:junit:1.1.2")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.3.0")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.3.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
}