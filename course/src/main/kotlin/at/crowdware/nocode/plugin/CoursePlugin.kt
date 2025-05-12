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

class CoursePlugin : NoCodePlugin, ExportPlugin, CreatePlugin {
    override val id = "course-plugin"
    override val label = "Course Generator"
    override val icon = "icon.svg"

    override fun create(name: String, appId: String, path: String, theme: String) {
        val dir = File("$path$name")
        dir.mkdirs()
        val pages = File("$path$name/pages")
        pages.mkdirs()
        val parts = File("$path$name/parts")
        parts.mkdirs()
        val videos = File("$path$name/videos")
        videos.mkdirs()
        val sounds = File("$path$name/sounds")
        sounds.mkdirs()
        val images = File("$path$name/images")
        images.mkdirs()

        val app = File("$path$name/app.sml")
        var appContent = """
                App {
                    smlVersion: "1.1"
                    name: "$name"
                    version: "1.0"
                    id: "$appId.$name"
                    icon: "icon.png"

                    Course {
                        lang: "en"

                        Topic {
                            label: "Topic 1"

                            Lecture {
                                label: "Lecture 1"
                                src: "lecture_1.sml"
                            }
                        }

                        Topic {
                            label: "Topic 2"

                            Lecture {
                                label: "Lecture 2"
                                src: "lecture_1.sml"
                            }
                        }
                    }

                """.trimIndent()

        appContent += if (theme == "Light")
            writeLightTheme()
        else
            writeDarkTheme()
        appContent += "}\n"
        app.writeText(appContent)

        val home = File("$path$name/pages/home.sml")
        val homeContent = """
            Page {
                padding: "8"

                Column {
                    padding: "8"

                    Markdown { text: "part:home" }
                }
            }
            """.trimIndent()
        home.writeText(homeContent)

        val lecture1 = File("$path$name/pages/lecture_1.sml")
        val lecture1Content = """
            Page {
                padding: "8"

                Column {
                    padding: "8"

                    Markdown { text: "part:lecture_1" }
                }
            }
            """.trimIndent()
        lecture1.writeText(lecture1Content)

        val lecture2 = File("$path$name/pages/lecture_2.sml")
        val lecture2Content = """
            Page {
                padding: "8"

                Column {
                    padding: "8"

                    Markdown { text: "part:lecture_2" }
                }
            }
            """.trimIndent()
        lecture2.writeText(lecture2Content)

        val partHome = File("$path$name/parts/home-en.md")
        val partHomeContent = """
            # Welcome to the course $name
            Please open the menu by tapping the ☰ hamburger icon in the top left corner and select a lesson from the list.

            You can close the menu again to have more space for the actual lesson.
            """.trimIndent()
        partHome.writeText(partHomeContent)

        val partLecture1 = File("$path$name/parts/lecture_1-en.md")
        val partLecture1Content = """
            # Lecture 1
            Lorem ipsum dolor
            """.trimIndent()
        partLecture1.writeText(partLecture1Content)

        val partLecture2 = File("$path$name/parts/lecture_2-en.md")
        val partLecture2Content = """
            # Lecture 2
            Lorem ipsum dolor
            """.trimIndent()
        partLecture2.writeText(partLecture2Content)
    }

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

fun writeDarkTheme(): String {
    var content = "\n"
    content += "\tTheme {\n"
    content += "\t\tprimary: \"#FFB951\"\n"
    content += "\t\tonPrimary: \"#452B00\"\n"
    content += "\t\tprimaryContainer: \"#633F00\"\n"
    content += "\t\tonPrimaryContainer: \"#FFDDB3\"\n"
    content += "\t\tsecondary: \"#DDC2A1\"\n"
    content += "\t\tonSecondary: \"#3E2D16\"\n"
    content += "\t\tsecondaryContainer: \"#56442A\"\n"
    content += "\t\tonSecondaryContainer: \"#FBDEBC\"\n"
    content += "\t\ttertiary: \"#B8CEA1\"\n"
    content += "\t\tonTertiary: \"#243515\"\n"
    content += "\t\ttertiaryContainer: \"#3A4C2A\"\n"
    content += "\t\tonTertiaryContainer: \"#D4EABB\"\n"
    content += "\t\terror: \"#FFB4AB\"\n"
    content += "\t\terrorContainer: \"#93000A\"\n"
    content += "\t\tonError: \"#690005\"\n"
    content += "\t\tonErrorContainer: \"#FFDAD6\"\n"
    content += "\t\tbackground: \"#1F1B16\"\n"
    content += "\t\tonBackground: \"#EAE1D9\"\n"
    content += "\t\tsurface: \"#1F1B16\"\n"
    content += "\t\tonSurface: \"#EAE1D9\"\n"
    content += "\t\tsurfaceVariant: \"#4F4539\"\n"
    content += "\t\tonSurfaceVariant: \"#D3C4B4\"\n"
    content += "\t\toutline: \"#9C8F80\"\n"
    content += "\t\tinverseOnSurface: \"#1F1B16\"\n"
    content += "\t\tinverseSurface: \"#EAE1D9\"\n"
    content += "\t\tinversePrimary: \"#825500\"\n"
    content += "\t\tsurfaceTint: \"#FFB951\"\n"
    content += "\t\toutlineVariant: \"#4F4539\"\n"
    content += "\t\tscrim: \"#000000\"\n"
    content += "\t}\n"
    return content
}

fun writeLightTheme(): String {
    var content = "\n"
    content += "\tTheme {\n"
    content += "\t\tprimary: \"#825500\"\n"
    content += "\t\tonPrimary: \"#FFFFFF\"\n"
    content += "\t\tprimaryContainer: \"#FFDDB3\"\n"
    content += "\t\tonPrimaryContainer: \"#291800\"\n"
    content += "\t\tsecondary: \"#6F5B40\"\n"
    content += "\t\tonSecondary: \"#FFFFFF\"\n"
    content += "\t\tsecondaryContainer: \"#FBDEBC\"\n"
    content += "\t\tonSecondaryContainer: \"#271904\"\n"
    content += "\t\ttertiary: \"#51643F\"\n"
    content += "\t\tonTertiary: \"#FFFFFF\"\n"
    content += "\t\ttertiaryContainer: \"#D4EABB\"\n"
    content += "\t\tonTertiaryContainer: \"#102004\"\n"
    content += "\t\terror: \"#BA1A1A\"\n"
    content += "\t\terrorContainer: \"#FFDAD6\"\n"
    content += "\t\tonError: \"#FFFFFF\"\n"
    content += "\t\tonErrorContainer: \"#410002\"\n"
    content += "\t\tbackground: \"#FFFBFF\"\n"
    content += "\t\tonBackground: \"#1F1B16\"\n"
    content += "\t\tsurface: \"#FFFBFF\"\n"
    content += "\t\tonSurface: \"#1F1B16\"\n"
    content += "\t\tsurfaceVariant: \"#F0E0CF\"\n"
    content += "\t\tonSurfaceVariant: \"#4F4539\"\n"
    content += "\t\toutline: \"#817567\"\n"
    content += "\t\tinverseOnSurface: \"#F9EFE7\"\n"
    content += "\t\tinverseSurface: \"#34302A\"\n"
    content += "\t\tinversePrimary: \"#FFB951\"\n"
    content += "\t\tsurfaceTint: \"#825500\"\n"
    content += "\t\tutlineVariant: \"#D3C4B4\"\n"
    content += "\t\tscrim: \"#000000\"\n"
    content += "\t}\n"
    return content
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
    val build = File(outputFolder, "app/build.gradle.kts")
    exchangePlaceholders(build , "applicationId = \"at.crowdware.coursereader\"", "applicationId = \"" + id + lang + "\"")

    val manifest = File(outputFolder, "app/src/main/AndroidManifest.xml")
    exchangePlaceholders(manifest , "android:label=\"CourseReader\"", "android:label=\"" + name + "\"")
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
