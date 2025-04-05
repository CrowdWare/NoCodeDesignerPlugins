package at.crowdware.nocode.plugin

import at.crowdware.nocode.utils.SmlNode
import at.crowdware.nocode.utils.getIntValue
import at.crowdware.nocode.utils.getStringValue
import at.crowdware.nocode.utils.parseSML
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class HTML5Plugin : SmlExportPlugin {
    override val id = "html5-plugin"
    override val label = "HTML5 Generator"
    override val icon = "icon.svg"

    override fun export(source: String, outputDir: File): ExportStatus {
        val outputFiles = mutableListOf<File>()
        val languages = listOf("de", "en", "pt", "fr", "eo", "es")
        for (lang in languages) {
            val partDir = File(source, "pages-$lang")
            partDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    println("Rendering: ${file.absoluteFile}")
                    val pageSml = file.readText()
                    val (parsedPage, _) = parseSML(pageSml)
                    val title = getStringValue(parsedPage!!, "title", "")
                    val outputFile = File(outputDir, "${file.name.substringBefore(".")}-$lang.html")
                    val content = StringBuilder()
                    content.append("<!doctype html>\n")
                    content.append("<html lang=\"${lang}\">\n")
                    content.append("<head>\n")
                    content.append("<meta charset=\"utf-8\">\n")
                    content.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                    content.append("<title>$title</title>\n")
                    content.append("<link rel=\"stylesheet\" href=\"assets/css/style.css\">\n")
                    content.append("<script src=\"assets/js/script.js\"></script>\n")
                    content.append("</head>\n")
                    content.append("<body>\n")
                    renderElements(parsedPage, content, source, lang)
                    content.append("</body>\n")
                    content.append("</html>\n")
                    outputFile.writeText(content.toString())
                    outputFiles.add(outputFile)
                }
            }
        }
        copyImages(source, outputDir)
        createAssets(outputDir)
        return ExportStatus(true, "Generated HTML", outputFiles)
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

fun createAssets(outputDir: File) {
    val css = """
    .row {
        display: flex;
        flex-direction: row;
    }
    .column {
        display: flex;
        flex-direction: column;
    }
    .fullwidth-button {
      width: 100%;
      box-sizing: border-box;
      padding: 10px;
      font-size: 1rem;
    }
    """.trimIndent()

    val targetDir = File(outputDir, "assets/css")
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
    val style = File(targetDir, "style.css")
    style.writeText(css)

    val js = """
        /* script.js */
    """.trimIndent()

    val jsDir = File(outputDir, "assets/js")
    if (!jsDir.exists()) {
        jsDir.mkdirs()
    }
    val script = File(jsDir, "script.js")
    script.writeText(js)
}

fun renderElements(node: SmlNode, content: StringBuilder, source: String, lang: String) {
    for (child in node.children) {
        when (child.name) {
            "Page" -> {
                renderElements(child, content, source, lang)
            }
            "Text" -> {
                val text = getStringValue(child, "text", "")
                if (text.isNotEmpty()) {
                    content.append("<p>$text</p>\n")
                }
            }
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
            }
            "Row" -> {
                content.append("<div style=\"display: flex; flex-direction: row;\">\n")
                renderElements(child, content, source, lang)
                content.append("</div>\n")
            }
            "Column" -> {
                content.append("<div style=\"display: flex; flex-direction: column;\">\n")
                renderElements(child, content, source, lang)
                content.append("</div>\n")
            }
            "Spacer" -> {
                val amount = getIntValue(child, "amount", 0)
                content.append("<div style=\"width:${amount}px; height:${amount}px;\"></div>\n")
            }
            else -> {
                println("Unknown element: ${child.name}")
            }
        }
    }
}

fun resolveLink(link: String, lang: String): String {
    return when {
        link.startsWith("page:") -> {
            val pageName = link.removePrefix("page:")
            "$pageName-$lang.html"
        }
        link.startsWith("web:") -> {
            val url = link.removePrefix("web:")
            // optional: https:// entfernen für Anzeige
            url.removePrefix("https://").removePrefix("http://")
            url
        }
        else -> link
    }
}

fun renderMarkdown(text: String): String {
    val options = MutableDataSet()
    options.set(HtmlRenderer.GENERATE_HEADER_ID, true)
    options.set(HtmlRenderer.RENDER_HEADER_ID, true)
    options.set(Parser.EXTENSIONS, listOf(TablesExtension.create()))

    val parser = Parser.builder(options).build()
    val document = parser.parse(text)
    val renderer = HtmlRenderer.builder(options).build()
    return renderer.render(document)
}