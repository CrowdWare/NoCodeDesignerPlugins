plugins {
    kotlin("jvm") version "1.8.21"
}

group = "at.crowdware.nocode"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Falls Plugins Templates oder Logging nutzen
    implementation("net.pwall.mustache:kotlin-mustache:0.12")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.register<Jar>("pluginJar") {
    archiveBaseName.set("bootstrap5-plugin")
    archiveVersion.set(project.version.toString())
    from(sourceSets.main.get().output)
    manifest {
        attributes(
            "Plugin-Class" to "at.crowdware.nocode.plugins.Bootstrap5Plugin"
        )
    }
}