
plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
}

loom {
    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp.set(false)
    }
}

repositories {
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
}
