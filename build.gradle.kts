group = "org.example"
version = "1.0-SNAPSHOT"

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    val kotlinVersion by System.getProperties()
    `maven-publish`
    id("tanvd.kosogor") version "1.0.13"
    id("org.jetbrains.dokka") version "1.6.21"
    kotlin("jvm").version(kotlinVersion.toString())
}

allprojects {
    apply {
        plugin("kotlin")

        tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    }
}