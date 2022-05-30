@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

import java.lang.Boolean as JavaBoolean

plugins {
    application
    java
}

application {
    mainClass.set("net.earthcomputer.multiconnect.tools.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.jetbrains:annotations:22.0.0")
    implementation("com.google.code.gson:gson:2.8.9")
}

tasks.getByName<JavaExec>("run") {
    workingDir(rootProject.file("."))
    debug = JavaBoolean.getBoolean("multiconnect.debugTools")
}
