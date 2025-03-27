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
            append("<html>\n<head>\n<title>${page.title}</title>\n</head>\n<body>\n")
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
            append("</body></html>")
        }
        outputFile.writeText(content)
        outputFiles.add(outputFile)
    }

    return ExportStatus(true, "Generated HTML", outputFiles)
}
}
