
plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("plugin.serialization") version "1.5.30"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io/")
    }
}

dependencies {
    implementation(project(":annotations"))
    implementation("com.github.Earthcomputer:kotlinx_serialization:9165d7c5") {
        exclude(module = "kotlinx-serialization-cbor-native")
        exclude(module = "kotlinx-serialization-core-native")
        exclude(module = "kotlinx-serialization-json-native")
        exclude(module = "kotlinx-serialization-properties-native")
        exclude(module = "kotlinx-serialization-protobuf-native")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
