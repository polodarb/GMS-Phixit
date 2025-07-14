import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
    id("com.google.protobuf")
    alias(libs.plugins.gms)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val requiresSigning = keystorePropertiesFile.exists()

android {
    namespace = "ua.polodarb.gmsphixit"
    compileSdk = 36

    signingConfigs {
        if (requiresSigning) {
            val keystoreProperties = Properties().apply {
                load(FileInputStream(keystorePropertiesFile))
            }
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "ua.polodarb.gmsphixit"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (requiresSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
        aidl = true
    }
    sourceSets {
        getByName("main") {
            aidl.srcDirs("src/main/aidl")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

tasks.register("fixAidlHeader") {
    doLast {
        val aidlDir = file("build/generated/aidl_source_output_dir")
        if (!aidlDir.exists()) return@doLast

        aidlDir.walkTopDown().forEach { file ->
            if (file.extension == "java") {
                val content = file.readText()
                val pattern = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL)
                val replacedContent = pattern.replace(content) { matchResult ->
                    matchResult.value.replace("\\", "/")
                }
                file.writeText(replacedContent)
            }
        }
    }
}

tasks.withType<com.android.build.gradle.tasks.AidlCompile>().configureEach {
    finalizedBy(tasks.named("fixAidlHeader"))
}


dependencies {

    // clarity
    implementation("com.microsoft.clarity:clarity-compose:3.4.1")

    // coil
    implementation(libs.bundles.coil)
    implementation("io.ktor:ktor-client-okhttp:3.1.1")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.performance) {
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }
    implementation(libs.firebase.cloud.messaging)

    // protobuf
    implementation("com.google.protobuf:protobuf-javalite:4.29.2")
//    implementation(libs.kotlinx.serialization.protobuf)


    // nav
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)

    // hilt
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // SQLite
    implementation(libs.requery.sqlite)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // libsu
    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.nio)

    // SplashScreen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.androidx.compose.core)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material)

}