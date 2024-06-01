pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
	repositories {
		maven("https://repo.nyon.dev/releases")
	}
	versionCatalogs {
		create("ichor") {
			from("fyi.pauli:ichor-catalog:1.6.3")
		}
	}
}

rootProject.name = "prorialize"