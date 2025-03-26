package at.crowdware.nocode.plugin

import at.crowdware.nocode.utils.App
import at.crowdware.nocode.plugin.SmlExportPlugin
import java.io.File

class Bootstrap5Plugin : SmlExportPlugin {
    override val id = "bootstrap5"
    override val label = "Bootstrap 5 Generator"
    override val icon = "icon.svg"

    override fun export(app: App, outputDir: File): ExportStatus {
        println("inside export")
        val outputFile = File(outputDir, app.name + ".html")
        val content = buildString {
            append("<html><head><title>${app.name}</title></head><body>")
            append("<h1>Hello from ${app.name}</h1>")
            //app.pages.forEach { page ->
            //    append("<h1>${page.title}</h1>")
            //    page.elements.forEach { el ->
            //        append("<p>${el.id}</p>")
            //    }
            //}
            append("</body></html>")
        }
        outputFile.writeText(content)
        return ExportStatus(true, "Generated HTML", listOf(outputFile))
    }
}