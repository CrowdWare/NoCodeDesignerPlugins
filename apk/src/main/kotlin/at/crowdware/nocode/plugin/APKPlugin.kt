package at.crowdware.nocode.plugin

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
            copyImages(source, outputFolder)

            val languages = listOf("de", "en", "pt", "fr", "eo", "es")
            for (lang in languages) {
                val partDir = File(source, "pages-$lang")
                partDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        println("Rendering: ${file.absoluteFile}")
                        val pageSml = file.readText()
                        val (parsedPage, _) = parseSML(pageSml)
                        val title = getStringValue(parsedPage!!, "title", "")
                        val fileName = file.name.substringBefore(".")
                        val outputFile = File(outputFolder, "/app/src/main/java/at/crowdware/nocodeapp/ui/$fileName-$lang.kt")
                        val content = StringBuilder()
                        content.append("package at.crowdware.nocodeapp.ui\n")
                        content.append("\n")
                        content.append("import androidx.compose.material3.Text\n")
                        content.append("\n")
                        content.append("\n")
                        content.append("@Composable\n")
                        content.append("fun " + fileName + lang + " {\n")
                        renderElements(parsedPage, content, source, lang, 1)
                        content.append("}\n")

                        outputFile.writeText(content.toString())
                        outputFiles.add(outputFile)
                    }
                }
            }
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

fun copyImages(source: String, outputDir: File) {
    val sourceDir = File(source, "images")
    val targetDir = File(outputDir,"assets/images")
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
    sourceDir.walkTopDown().forEach { file ->
        if (file.isFile) {
            val targetFile = File(targetDir, file.name)
            Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

fun renderElements(node: SmlNode, content: StringBuilder, source: String, lang: String, tabs: Int) {
    for (child in node.children) {
        when (child.name) {
            "Text" -> {
                val text = getStringValue(child, "text", "")
                if (text.isNotEmpty()) {
                    content.append("\t".repeat(tabs))
                    content.append("Text(text = \"$text\")\n")
                }
            }
            /* 
            "Markdown" -> {
                val text = getStringValue(child, "text", "")
                if (text.isNotEmpty()) {
                    val md = renderMarkdown(text)
                    content.append("<p>$md</p>\n")
                }
                val part = getStringValue(child, "part", "")
                if (part.isNotEmpty()) {
                    val file = File(source, "parts-$lang/$part")
                    try {
                        val txt = file.readText(Charsets.UTF_8)
                        val md = renderMarkdown(txt)
                        content.append("<p>$md</p>\n")
                    } catch(e: Exception) {
                        println("An error occured: ${e.message}")
                    }
                }
            }
            
            "Image" -> {
                val src = getStringValue(child, "src", "")
                content.append("<img src=\"assets/images/$src\">\n")
            }
            "Button" -> {
                val label = getStringValue(child, "label", "")
                val link = getStringValue(child, "link", "")
                val href = resolveLink(link, lang)
                content.append("<a href=\"$href\" style=\"flex: 1;\"><button class=\"fullwidth-button\">$label</button></a>\n")
            }*/
            "Row" -> {
                content.append("\t".repeat(tabs))
                content.append("Row() {\n")
                renderElements(child, content, source, lang, tabs + 1)
                content.append("\t".repeat(tabs))
                content.append("}\n")
            }
            "Column" -> {
                content.append("\t".repeat(tabs))
                content.append("Column() {\n")
                renderElements(child, content, source, lang, tabs + 1)
                content.append("\t".repeat(tabs))
                content.append("}\n")
            }
            /*
            "Spacer" -> {
                val amount = getIntValue(child, "amount", 0)
                content.append("<div style=\"width:${amount}px; height:${amount}px;\"></div>\n")
            }*/
            else -> {
                println("Unknown element: ${child.name}")
            }
        }
    }
}