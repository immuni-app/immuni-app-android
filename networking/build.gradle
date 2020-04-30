apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-android-extensions'
apply plugin: 'kotlin-kapt'

android {
    compileSdkVersion rootProject.compileSdkVersion
    buildToolsVersion rootProject.buildToolsVersion


    defaultConfig {
        minSdkVersion rootProject.minSdkVersion
        targetSdkVersion rootProject.targetSdkVersion
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles 'consumer-rules.pro'
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).all {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    kotlinOptions {
        jvmTarget = '1.8'
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation project(':extensions')
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesCoreVersion"
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesAndroidVersion"
    implementation "androidx.appcompat:appcompat:$appcompatVersion"
    implementation "androidx.core:core-ktx:$ktxCoreVersion"

    // Lifecycle
    implementation "androidx.lifecycle:lifecycle-extensions:$lifecycleVersion"

    //Moshi
    implementation "com.squareup.moshi:moshi-kotlin:$moshiVersion"
    kapt "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
    kaptTest "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion"
    implementation "com.squareup.moshi:moshi-adapters:$moshiVersion"

    //Retrofit2
    implementation "com.squareup.retrofit2:retrofit:$retrofitVersion"
    implementation "com.squareup.retrofit2:converter-moshi:$retrofitVersion"

    //Okhttp3
    implementation "com.squareup.okhttp3:okhttp:$okHttpVersion"
    implementation "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"

    //Conscrypt
    implementation "org.conscrypt:conscrypt-android:$conscryptVersion" // For certificate pinning

    testImplementation "junit:junit:$jUnitVersion"
    testImplementation "io.mockk:mockk:$mockkVersion"
    testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesCoreVersion"
    androidTestImplementation "androidx.test.ext:junit:$androidxTestVersion"
}
