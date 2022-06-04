import net.earthcomputer.multiconnect.buildscript.BuildUtils

plugins {
    id("fabric-loom") version "0.12-SNAPSHOT"
    id("maven-publish")
    id("signing")
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

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.WARN
        inputs.property("version", rootProject.version)

        from(sourceSets.main.get().resources.srcDirs) {
            include("fabric.mod.json")
            expand("version" to rootProject.version)
        }

        from(sourceSets.main.get().resources.srcDirs) {
            exclude("fabric.mod.json")
        }
    }

    jar {
        archiveClassifier.set("api-dev")
        from(rootProject.file("LICENSE"))
    }

    remapJar {
        archiveClassifier.set("api")
    }

    register<Jar>("sourcesJar") {
        dependsOn("classes")
        archiveClassifier.set("api-sources")
        from(sourceSets.main.get().allSource)
    }

    register<Jar>("javadocJar") {
        archiveClassifier.set("api-javadoc")
        from(javadoc)
    }
}

publishing {
    repositories(BuildUtils.repositoryHandler(rootProject))

    publications {
        register<MavenPublication>("api") {
            groupId = rootProject.group.toString()
            artifactId = "multiconnect-api"
            artifact(tasks.remapJar) {
                builtBy(tasks.remapJar)
                classifier = ""
            }
            artifact(tasks.getByName("sourcesJar")) {
                builtBy(tasks.getByName("sourcesJar"))
                classifier = "sources"
            }
            artifact(tasks.getByName("javadocJar")) {
                builtBy(tasks.getByName("javadocJar"))
                classifier = "javadoc"
            }
            BuildUtils.doConfigurePom(this)
            pom.name.set("multiconnect-api")
            pom.description.set("The multiconnect API")
        }
    }
}

signing {
    sign(publishing.publications.getByName("api"))
}
