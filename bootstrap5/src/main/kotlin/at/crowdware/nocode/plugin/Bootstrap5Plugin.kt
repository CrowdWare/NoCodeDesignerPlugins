package at.crowdware.nocode.plugin

import at.crowdware.nocode.utils.PartElement
import at.crowdware.nocode.utils.SmlNode
import at.crowdware.nocode.utils.getStringValue
import at.crowdware.nocode.utils.parseSML
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import net.pwall.mustache.Template
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory

class Bootstrap5Plugin : SmlExportPlugin {
    override val id = "bootstrap5-plugin"
    override val label = "Bootstrap 5 Generator"
    override val icon = "icon.svg"

    override fun export(source: String, outputDir: File): ExportStatus {
        val outputFiles = mutableListOf<File>()
        val languages = listOf("de", "en", "pt", "fr", "eo", "es")
        for (lang in languages) {
            val partDir = File(source, "pages-$lang")
            partDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val pageSml = file.readText()
                    val (parsedPage, _) = parseSML(pageSml)
                    val title = getStringValue(parsedPage!!, "title", "")
                    val outputFile = File(outputDir, "${file.name.substringBefore(".")}-$lang.html")
                    val content = buildString {
                        append("<!doctype html>\n")
                        append("<html lang=\"${lang}\">\n")
                        append("<head>\n")
                        append("<meta charset=\"utf-8\">\n")
                        append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                        append("<title>$title</title>\n")
                        append("<link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH\" crossorigin=\"anonymous\">\n")
                        append("</head>\n")
                        append("<body>\n")
                        append("<h1>Hello from $title</h1>\n")
                        for (child in parsedPage.children) {
                            val element = getStringValue(child, "name", "")
                            val text = getStringValue(child, "text", "")
                            when (element) {
                                "Text" -> {
                                    append("<p>" + text + "</p>\n")
                                }
                                "Markdown" -> {
                                    append("<p>" + text + "</p>\n")
                                }
                                else -> {
                                    // this is just a sample plugin implementation
                                    // normaly we at CrowdWare want to get rid of HTML in general
                                    // so help yourself and extend this plugin if you really need HTML
                                    println("Unknown element: $element")
                                }
                            }
                        }
                        append("<script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz\" crossorigin=\"anonymous\"></script>\n")
                        append("</body>\n")
                        append("</html>\n")
                    }
                    outputFile.writeText(content)
                    outputFiles.add(outputFile)
                }
            }
        }
        return ExportStatus(true, "Generated HTML", outputFiles)
    }
}