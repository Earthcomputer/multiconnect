
plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.31"
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
    implementation("com.google.guava:guava:31.0.1-jre")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.create<JavaExec>("compileTranslator") {
    group = "build"
    classpath(sourceSets.main.get().runtimeClasspath)
    main = "net.earthcomputer.multiconnect.compiler.Main"
    args(
        "${rootProject.buildDir}/translatorJsons",
        rootProject.file("data").absolutePath,
        rootProject.sourceSets.getByName("generated").java.srcDirs.iterator().next().absolutePath
    )
}
