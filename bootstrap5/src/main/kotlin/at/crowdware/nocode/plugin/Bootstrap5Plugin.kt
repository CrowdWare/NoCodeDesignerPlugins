package at.crowdware.nocode.plugin

import at.crowdware.nocode.utils.App
import at.crowdware.nocode.utils.Page
import at.crowdware.nocode.plugin.SmlExportPlugin
import java.io.File
import kotlin.collections.List

class Bootstrap5Plugin : SmlExportPlugin {
    override val id = "bootstrap5"
    override val label = "Bootstrap 5 Generator"
    override val icon = "icon.svg"

override fun export(app: App, pages: List<Page>, outputDir: File): ExportStatus {
    println("inside export")

    val outputFiles = mutableListOf<File>()

    for (page in pages) {
        val outputFile = File(outputDir, "${page.title}.html")
        val content = buildString {
            append("<html><head><title>${page.title}</title></head><body>")
            append("<h1>Hello from ${page.title}</h1>")
            append("</body></html>")
        }
        outputFile.writeText(content)
        outputFiles.add(outputFile)
    }

    return ExportStatus(true, "Generated HTML", outputFiles)
}
}
