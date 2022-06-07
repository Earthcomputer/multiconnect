package net.earthcomputer.multiconnect.buildscript

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.Publication
import org.gradle.api.publish.maven.MavenPublication

class BuildUtils {
    static Closure<Publication> configurePom = {
        packaging 'jar'
        inceptionYear = '2019'
        licenses {
            license {
                name = 'MIT'
                url = 'https://github.com/Earthcomputer/multiconnect/blob/master/LICENSE'
            }
        }
        url = 'https://github.com/Earthcomputer/multiconnect'
        issueManagement {
            url = 'https://github.com/Earthcomputer/multiconnect/issues'
        }
        scm {
            connection = 'scm:git:https://github.com/Earthcomputer/multiconnect'
            developerConnection = 'scm:git:https://github.com/Earthcomputer/multiconnect'
            url = 'https://github.com/Earthcomputer/multiconnect'
        }
        developers {
            developer {
                name = 'Joseph Burton'
                id = 'Earthcomputer'
                email = 'burtonjae@hotmail.co.uk'
                roles = ['Main Developer']
            }
        }
    }

    static void doConfigurePom(MavenPublication pub) {
        pub.pom configurePom
    }

    static Action<RepositoryHandler> repositoryHandler(Project project) {
        return {
            it.maven {
                name = 'nexus'
                url = isBeta(project) ? 'https://s01.oss.sonatype.org/content/repositories/snapshots/' : 'https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/'
                credentials {
                    username(project.hasProperty('ossrhUser') ? project.property('ossrhUser') : 'foo')
                    password(project.hasProperty('ossrhPass') ? project.property('ossrhPass') : 'bar')
                }
            }
            it.mavenLocal()
        }
    }

    static boolean isBeta(Project project) {
        return project.version.contains('pre') || project.version.contains('beta')
    }
}
