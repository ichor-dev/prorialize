@file:OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    kotlin("multiplatform") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"

    id("com.github.breadmoirai.github-release") version "2.4.1"

    `maven-publish`
    signing
}

group = "fyi.pauli"
version = "1.0.0"

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

githubRelease {
    token(findProperty("github.token")?.toString())

    owner("ichor-dev")
    repo("ichor")
    tagName("v${project.version}")
    overwrite(true)
    releaseAssets(tasks["kotlin"].outputs.files)
    targetCommitish("main")
}

publishing {
    repositories {
        maven {
            name = "nyon"
            url = uri("https://repo.nyon.dev/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "fyi.pauli"
            artifactId = "prorialize"
            version = project.version.toString()
            from(components["kotlin"])
        }
    }
}

signing {
    sign(publishing.publications)
}