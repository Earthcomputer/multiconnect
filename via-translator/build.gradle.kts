
plugins {
    id("fabric-loom") version "1.0-SNAPSHOT"
}

loom {
    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp.set(false)
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.viaversion.com/")
    }
    maven {
        url = uri("https://maven.parchmentmc.org/")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.property("parchment_mcversion")}:${rootProject.property("parchment_version")}@zip")
    })
    implementation(project(":translator-api"))
    implementation("com.viaversion:viaversion:${rootProject.property("viaversion_version")}")
    implementation("org.yaml:snakeyaml:${rootProject.property("snakeyaml_version")}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}
