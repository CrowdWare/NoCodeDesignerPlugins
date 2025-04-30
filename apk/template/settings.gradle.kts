pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "FreeBookReader"
include(":app")
include(":nocodelibmobile")
project(":nocodelibmobile").projectDir = file("nocodelibmobile")