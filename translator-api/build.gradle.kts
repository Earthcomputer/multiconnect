
plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.netty:netty-transport:4.1.77.Final")
    api("org.jetbrains:annotations:23.0.0")
}
