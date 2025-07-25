// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.10.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    id ("org.jetbrains.kotlin.jvm") version "1.7.20" apply false
    id ("org.jetbrains.kotlin.plugin.serialization") version "1.7.20" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}