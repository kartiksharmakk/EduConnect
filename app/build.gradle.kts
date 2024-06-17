plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin") version "2.7.7"
    id("com.github.ben-manes.versions") version "0.39.0"
    id("kotlin-kapt")
}


android {
    namespace = "com.kartik.tutordashboard"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kartik.tutordashboard"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
    }
}

dependencies {

    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")
    var composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation ("androidx.compose.runtime:runtime")
    implementation ("androidx.compose.ui:ui")
    implementation ("androidx.compose.foundation:foundation")
    implementation ("androidx.compose.foundation:foundation-layout")
    implementation ("androidx.compose.material:material")
    implementation ("androidx.compose.runtime:runtime-livedata")
    implementation ("androidx.compose.ui:ui-tooling")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose")
    implementation("androidx.compose.material:material-icons-extended")
    implementation ("androidx.activity:activity-compose")
    implementation("io.coil-kt:coil-compose:2.1.0")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.hbb20:ccp:2.7.1")
    implementation("de.nycode:bcrypt:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")

    implementation("androidx.lifecycle:lifecycle-common-java8:2.4.0")
    implementation("com.github.Kwasow:BottomNavigationCircles-Android:1.2")

    implementation("com.google.zxing:core:3.3.3")
    //implementation("com.google.zxing:android-core:3.3.3")
    //implementation("com.google.zxing:android-integration:3.3.3")
    implementation("com.google.zxing:javase:3.3.3")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.github.yalantis:ucrop:2.2.6")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.google.code.gson:gson:2.10.1")
   // implementation("com.github.quickpermissions:quickpermissions-kotlin:0.4.0")
    implementation("com.journeyapps:zxing-android-embedded:4.2.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")


    // Room

    implementation ("androidx.room:room-runtime:2.6.1")
    kapt  ("androidx.room:room-compiler:2.6.1")
// Kotlin Extensions and Coroutines support for Room
    implementation ("androidx.room:room-ktx:2.6.1")


    implementation ("com.firebaseui:firebase-ui-database:8.0.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
}