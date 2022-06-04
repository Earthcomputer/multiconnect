
plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
}

loom {
    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp.set(false)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${rootProject.property("yarn_mappings")}:v2")
}
