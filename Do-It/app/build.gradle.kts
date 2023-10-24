plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
}

android {
    namespace = "team.doit.do_it"
    compileSdk = 33

    defaultConfig {
        applicationId = "team.doit.do_it"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        configurations.all {
            resolutionStrategy {
                force("androidx.emoji2:emoji2-views-helper:1.3.0")
                force("androidx.emoji2:emoji2:1.3.0")
            }
        }
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.2")
    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
    implementation("com.google.firebase:firebase-storage-ktx:20.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.5.3")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation ("com.mikhaellopez:circularimageview:4.3.1")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation(platform("com.google.firebase:firebase-auth:22.1.2"))
    implementation(platform("com.google.firebase:firebase-database:20.0.3"))
    implementation(platform("com.google.firebase:firebase-firestore:24.8.1"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")
    implementation("com.firebaseui:firebase-ui-storage:7.2.0")
    implementation("com.firebaseui:firebase-ui-database:8.0.2")

    // Images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    //Stripe
    implementation("com.stripe:stripecardscan:20.33.0")
    implementation("com.stripe:stripe-android:20.33.0")

    implementation("com.squareup.okhttp3:okhttp:4.9.2")

    // Google AdMob
    implementation ("com.google.android.gms:play-services-ads:22.4.0")

    implementation("androidx.paging:paging-runtime:3.2.1")
}