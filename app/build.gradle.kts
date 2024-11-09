plugins {
    //alias(libs.plugins.android.application)
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "student.inti.assignment"
    compileSdk = 34

    defaultConfig {
        applicationId = "student.inti.assignment"
        minSdk = 26
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

}

dependencies {
    implementation(libs.androidx.preference)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.inappmessaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))


    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-firestore-ktx:24.3.0")


    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    implementation("com.google.firebase:firebase-auth-ktx:22.0.0")

    // For Card view
    implementation(libs.androidx.cardview)
    implementation("androidx.cardview:cardview:1.0.0")

    // Chart and graph library
    implementation(libs.eazegraph)
    implementation("com.github.blackfizz:eazegraph:1.2.2@aar")
    implementation(libs.library)
    implementation("com.nineoldandroids:library:2.4.0")

    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.core:core-ktx:1.6.0")
    implementation ("androidx.core:core:1.7.0")

}

apply(plugin = "com.google.gms.google-services")


