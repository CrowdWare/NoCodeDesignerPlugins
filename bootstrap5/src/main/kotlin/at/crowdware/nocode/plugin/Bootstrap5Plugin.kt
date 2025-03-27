package at.crowdware.nocode.plugin

import at.crowdware.nocode.utils.App
import at.crowdware.nocode.utils.Page
import at.crowdware.nocode.utils.UIElement
import at.crowdware.nocode.utils.UIElement.*
import at.crowdware.nocode.plugin.SmlExportPlugin
import java.io.File
import kotlin.collections.List

class Bootstrap5Plugin : SmlExportPlugin {
    override val id = "bootstrap5-plugin"
    override val label = "Bootstrap 5 Generator"
    override val icon = "icon.svg"

override fun export(app: App, pages: List<Page>, outputDir: File): ExportStatus {
    val outputFiles = mutableListOf<File>()

    for (page in pages) {
        val outputFile = File(outputDir, "${page.title}.html")
        val content = buildString {
            append("<!doctype html>\n")
            append("<html lang=\"$page.language\">\n")
            append("<head>\n")
            append("<meta charset=\"utf-8\">\n")
            append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
            append("<title>${page.title}</title>\n")
            append("<link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH\" crossorigin=\"anonymous\">\n")
            append("</head>\n")
            append("<body>\n")
            append("<h1>Hello from ${page.title}</h1>\n")
            for (element in page.elements) {
                when (element) {
                    is TextElement -> {
                        append("<p>" + element.text + "</p>\n")
                    }
                    is MarkdownElement -> {
                        append("<p>" + element.text + "</p>\n")
                    }
                    else -> {
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

    return ExportStatus(true, "Generated HTML", outputFiles)
}
}