
plugins {
    id("fabric-loom") version "1.0-SNAPSHOT"
    id("com.modrinth.minotaur") version "1.2.1"
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
    maven {
        url = uri("https://jitpack.io/")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.property("parchment_mcversion")}:${rootProject.property("parchment_version")}@zip")
    })
    modImplementation("com.github.Earthcomputer:fabric-loader:${rootProject.property("loader_version")}")
    implementation(project(":translator-api"))
    implementation("com.viaversion:viaversion:${rootProject.property("viaversion_version")}") {
        isTransitive = false
    }
    implementation("org.yaml:snakeyaml:${rootProject.property("snakeyaml_version")}")
    include("org.yaml:snakeyaml:${rootProject.property("snakeyaml_version")}")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

tasks.register<com.modrinth.minotaur.TaskModrinthUpload>("publishModrinth") {
    dependsOn(tasks.getByName("remapJar"))

    onlyIf {
        project.hasProperty("modrinthKey")
    }

    token = if (project.hasProperty("modrinthKey")) project.property("modrinthKey").toString() else "foo"
    projectId = "MNhf9veJ"
    versionName = project.version.toString()
    versionNumber = project.version.toString()
    uploadFile = tasks.getByName("remapJar")
    addGameVersion(rootProject.property("minecraft_version").toString())
    addLoader("fabric")
    addLoader("quilt")
}

tasks {
    jar {
        from("LICENSE.md")
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.WARN

        inputs.property("version", project.version)
        from(sourceSets.main.get().resources.srcDirs) {
            include("fabric.mod.json")
            expand("version" to project.version)
        }
        from(sourceSets.main.get().resources.srcDirs) {
            exclude("fabric.mod.json")
        }
    }
}
