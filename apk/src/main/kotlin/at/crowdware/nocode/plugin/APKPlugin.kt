package at.crowdware.nocode.plugin

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import at.crowdware.nocode.utils.SmlNode
import at.crowdware.nocode.utils.getIntValue
import at.crowdware.nocode.utils.getStringValue
import at.crowdware.nocode.utils.parseSML
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class APKPlugin : SmlExportPlugin {
    override val id = "apk-plugin"
    override val label = "APK Generator"
    override val icon = "icon.svg"

    override fun export(source: String, outputDir: File): ExportStatus {
        val outputFiles = mutableListOf<File>()

        val appFile = File(source, "app.sml")
        val appSml = appFile.readText()
        val (parsedApp, _) = parseSML(appSml)
        if (parsedApp != null) {
            val name = getStringValue(parsedApp, "name", "")
            val outputFolder = File(outputDir, "apk-plugin/$name")
            outputFolder.mkdirs()
            copyTemplate(outputFolder)
            copySources(File(source, "app.sml"), outputFolder)
            copySources(File(source, "images"), outputFolder)
            copySources(File(source, "pages"), outputFolder)
            copySources(File(source, "parts"), outputFolder)
            copySources(File(source, "translations"), outputFolder)
            changeAppId(getStringValue(parsedApp, "id", ""), getStringValue(parsedApp, "name", ""), outputFolder)
        }
        return ExportStatus(true, "Generated APK", outputFiles)
    }
}

fun copyTemplate(outputDir: File) {
    val jarPath = File(APKPlugin::class.java.protectionDomain.codeSource.location.toURI())

    // Von der JAR-Datei nach oben navigieren zum Plugin-Root
    val pluginDir = jarPath.parentFile.parentFile.parentFile
    val sourceDir = File(pluginDir, "template")

    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        throw IllegalStateException("Template-Verzeichnis nicht gefunden: ${sourceDir.absolutePath}")
    }

    sourceDir.walkTopDown().forEach { file ->
        if (file.isFile) {
            // Relativen Pfad innerhalb von template berechnen
            val relativePath = file.relativeTo(sourceDir)
            val targetFile = File(outputDir, relativePath.path)

            // Zielordner erstellen, falls nötig
            targetFile.parentFile.mkdirs()

            // Datei kopieren
            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
    // gradlew ausführbar machen, wenn vorhanden
    val gradlewFile = File(outputDir, "gradlew")
    if (gradlewFile.exists()) {
        gradlewFile.setExecutable(true)
    }
}

fun copySources(source: File, outputDir: File) {
    val baseTargetDir = File(outputDir, "app/src/main/assets")
    if (!baseTargetDir.exists()) {
        baseTargetDir.mkdirs()
    }
    if (source.isDirectory) {
        val targetDir = File(baseTargetDir, source.name)
        source.walkTopDown().forEach { file ->
            if (file.isFile) {
                val relativePath = file.relativeTo(source).path
                val targetFile = File(targetDir, relativePath)
                targetFile.parentFile?.mkdirs()
                Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    } else if (source.isFile) {
        val targetFile = File(baseTargetDir, source.name)
        Files.copy(source.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}

fun reverseUrl(url: String): String {
    return url.split(".").reversed().joinToString(".")
}

fun changeAppId(id: String, name: String, outputFolder: File) {
    val mainActivity = File(outputFolder, "app/src/main/java/at/crowdware/freebookreader/MainActivity.kt")
    exchangePlaceholders(mainActivity , "https://crowdware.github.io/FreeBookReader/app.sml", "https://" +  reverseUrl(id) + "/app.sml")

    val baseComposeActivity = File(outputFolder, "nocodelibmobile/src/main/java/at/crowdware/nocodelibmobile/BaseComposeActivity.kt")
    exchangePlaceholders(baseComposeActivity , "ContentCache/crowdware_github_io/NoCode", "ContentCache/" + reverseUrl(id).replace(".", "_"))

    val build = File(outputFolder, "app/build.gradle.kts")
    exchangePlaceholders(build , "applicationId = \"at.crowdware.freebookreader\"", "applicationId = \"" + id + "\"")

    val manifest = File(outputFolder, "app/src/main/AndroidManifest.xml")
    exchangePlaceholders(manifest , "android:label=\"FreeBookReader\"", "android:label=\"" + name + "\"")
    
}

fun exchangePlaceholders(file: File, placeHolder: String, newValue: String) {
    if (!file.exists()) return
    val content = file.readText()
    val replaced = content.replace(placeHolder, newValue)
    file.writeText(replaced)
}