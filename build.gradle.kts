@file:OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)

import org.gradle.internal.logging.text.StyledTextOutput.Style.Failure
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    kotlin("multiplatform") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"

    id("com.github.breadmoirai.github-release") version "2.4.1"

    `maven-publish`
    signing
}

group = "fyi.pauli"
version = "1.0.0"
description = "Kotlin.serialization library for the Minecraft protocol."
val authors = listOf("btwonion", "kxmpxtxnt")
val isSnapshot = false

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.1")
                api("org.jetbrains.kotlinx:kotlinx-io-core:0.3.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
            }
        }
    }
}

configure<SigningExtension> {
    val publications = extensions.getByType<PublishingExtension>().publications
    val publicationCount = publications.size
    val message = "The following $publicationCount publication(s) are getting signed: ${publications.map(Named::getName)}"
    val style = when (publicationCount) {
        0 -> Failure
        else -> Success
    }
    serviceOf<StyledTextOutputFactory>().create("signing").style(style).println(message)
    sign(*publications.toTypedArray())
}

val githubRepo = "ichor-dev/prorialize"
githubRelease {
    token(findProperty("github.token")?.toString())

    val (owner, repo) = githubRepo.split('/')
    owner(owner)
    repo(repo)
    tagName("v${project.version}")
    overwrite(true)
    releaseAssets(components["kotlin"])
    targetCommitish("main")
}

publishing {
    repositories {
        maven {
            name = "ossrh"
            credentials(PasswordCredentials::class)
            setUrl(
                if (!isSnapshot) "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
                else "https://s01.oss.sonatype.org/content/repositories/snapshots"
            )
        }
    }

    publications {
        register<MavenPublication>(project.name) {
            from(components["kotlin"])

            this.groupId = project.group.toString()
            this.artifactId = project.name
            this.version = version.toString()

            pom {
                name.set(project.name)
                description.set(project.description)

                developers {
                    authors.forEach {
                        developer { name.set(it) }
                    }
                }

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                url.set("https://github.com/")

                scm {
                    connection.set("scm:git:git://github.com/${githubRepo}.git")
                    url.set("https://github.com/${githubRepo}")
                }
            }
        }
    }
}