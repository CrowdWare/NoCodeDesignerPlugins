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

class CoursePlugin : NoCodePlugin, ExportPlugin {
    override val id = "course-plugin"
    override val label = "Course Generator"
    override val icon = "icon.svg"

    override suspend fun export(source: String, outputDir: File, onLog: (String) -> Unit): ExportStatus {
        onLog("export started...")
        val outputFiles = mutableListOf<File>()
        val appFile = File(source, "app.sml")
        val appSml = appFile.readText()
        val (parsedApp, _) = parseSML(appSml)
        if (parsedApp != null) {
            val name = getStringValue(parsedApp, "name", "")
            for (node in parsedApp.children) {
                if (node.name == "Course") {
                    val lang = getStringValue(node, "lang", "")
                    val outputFolder = File(outputDir, lang)
                    outputFolder.mkdirs()
                    onLog("copying template")
                    copyTemplate(outputFolder)
                    onLog("copying source")
                    copySources(File(source, "app.sml"), outputFolder)
                    copySources(File(source, "images"), outputFolder)
                    copySources(File(source, "pages"), outputFolder)
                    copySources(File(source, "parts"), outputFolder)
                    copySources(File(source, "translations"), outputFolder)
                    changeAppId(
                        getStringValue(parsedApp, "id", ""),
                        getStringValue(parsedApp, "name", ""),
                        outputFolder,
                        lang
                    )
                    setLanguage(outputFolder, lang)
                    setPrecashed(outputFolder)
                    onLog("starting to build apk")
                    //val exitCode = runGradleBuild(outputFolder) { line ->
                    //    println(">> $line")
                    //    onLog(line)
                    //}
                }
            }
        }
        return ExportStatus(true, "Generated APK", outputFiles)
    }
}

suspend fun runGradleBuild(projectDir: File, onLog: suspend (String) -> Unit): Int {
    val gradlew = File(projectDir, if (System.getProperty("os.name").startsWith("Windows")) "gradlew.bat" else "gradlew")
    if (!gradlew.exists()) throw IllegalArgumentException("gradlew not found in ${projectDir.absolutePath}")

    val process = ProcessBuilder(gradlew.absolutePath, "assembleRelease")
        .directory(projectDir)
        .redirectErrorStream(true)
        .start()

    process.inputStream.bufferedReader().useLines { lines ->
        for (line in lines) {
            onLog(line)
        }
    }

    return process.waitFor()
}

fun copyTemplate(outputDir: File) {
    val jarPath = File(CoursePlugin::class.java.protectionDomain.codeSource.location.toURI())

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

fun changeAppId(id: String, name: String, outputFolder: File, lang: String) {
    val mainActivity = File(outputFolder, "app/src/main/java/at/crowdware/freebookreader/MainActivity.kt")
    exchangePlaceholders(mainActivity , "https://crowdware.github.io/FreeBookReader/app.sml", "https://" + reverseUrl(id) + lang + "/app.sml")

    val baseComposeActivity = File(outputFolder, "nocodelibmobile/src/main/java/at/crowdware/nocodelibmobile/BaseComposeActivity.kt")
    exchangePlaceholders(baseComposeActivity , "ContentCache/crowdware_github_io/NoCode", "ContentCache/" + reverseUrl(id).replace(".", "_") + lang)

    val build = File(outputFolder, "app/build.gradle.kts")
    exchangePlaceholders(build , "applicationId = \"at.crowdware.freebookreader\"", "applicationId = \"" + id + lang + "\"")

    val manifest = File(outputFolder, "app/src/main/AndroidManifest.xml")
    exchangePlaceholders(manifest , "android:label=\"FreeBookReader\"", "android:label=\"" + name + "\"")
    
}

fun setLanguage(outputFolder: File, lang: String) {
    val mainActivity = File(outputFolder, "app/src/main/java/at/crowdware/freebookreader/MainActivity.kt")
    insertAfter(mainActivity , "LocaleManager.init(applicationContext, resources)", "\n\t\t\tLocaleManager.setLocale(applicationContext, \"" + lang + "\")")
}

fun setPrecashed(outputFolder: File) {
    val app = File(outputFolder, "app/src/main/assets/app.sml")
    insertAfter(app, "App {", "\n\tprecached: true")
}

fun exchangePlaceholders(file: File, placeHolder: String, newValue: String) {
    if (!file.exists()) return
    val content = file.readText()
    val replaced = content.replace(placeHolder, newValue)
    file.writeText(replaced)
}

fun insertAfter(file: File, searchFor: String, insertValue: String) {
    if (!file.exists()) return
    val content = file.readText()
    val index = content.indexOf(searchFor)
    if (index == -1) return // nothing found

    // Insert after the search string
    val newContent = buildString {
        append(content.substring(0, index + searchFor.length))
        append(insertValue)
        append(content.substring(index + searchFor.length))
    }

    file.writeText(newContent)
}


fun getLanguages(source: String): List<String> {
    val languages = mutableSetOf<String>()

    val regex = Regex("-(.+)\\.(sml|md)")

    val dirsAndExtensions = listOf(
        "translations" to "sml",
        "parts" to "md"
    )

    for ((dirName, expectedExtension) in dirsAndExtensions) {
        val dir = File(source, dirName)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()
                ?.filter { it.isFile && it.name.contains("-") && it.name.endsWith(".$expectedExtension") }
                ?.forEach { file ->
                    regex.find(file.name)?.groupValues?.get(1)?.let { lang ->
                        languages.add(lang)
                    }
                }
        }
    }
    return languages.sorted()
}
