plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.10"
}

group = "at.crowdware.nocode"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    // Falls Plugins Templates oder Logging nutzen
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.ui)
 
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("net.pwall.mustache:kotlin-mustache:0.12")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
}

tasks.register<Jar>("pluginJar") {
    archiveBaseName.set("epub3-plugin")
    archiveVersion.set(project.version.toString())
    from(sourceSets.main.get().output)
    manifest {
        attributes(
            "Plugin-Class" to "at.crowdware.nocode.plugins.Epub3Plugin"
        )
    }
}