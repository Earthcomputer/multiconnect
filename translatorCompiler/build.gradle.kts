
plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotations"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.google.guava:guava:31.1-jre")
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
