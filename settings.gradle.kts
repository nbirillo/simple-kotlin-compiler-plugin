
rootProject.name = "kotlin-compiler-plugin-example"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    }
}

include(
    "compiler-plugin"
)

