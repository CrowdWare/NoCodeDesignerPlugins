package at.crowdware.nocode.plugin


import at.crowdware.nocode.utils.*
import at.crowdware.nocode.plugin.SmlExportPlugin
import java.io.File
import kotlin.collections.List

class Epub3Plugin : SmlExportPlugin {
    override val id = "epub3-plugin"
    override val label = "Epub 3 Generator"
    override val icon = "icon.svg"

    override fun export(app: App, pages: List<SmlNode>, parts: List<PartElement>, outputDir: File): ExportStatus {
    val outputFiles = mutableListOf<File>()

    for (page in pages) {
        val title = getStringValue(page, "title", "")
        val outputFile = File(outputDir, "$title.html")
        val content = buildString {
            
            append("$title\n")
            for (child in page.children) {
                val text = getStringValue(child, "text", "")
                
                when (child.name) {
                    "Text" -> {
                        append(text + "\n")
                    }
                    "Markdown" -> {
                        append(text + "\n")
                    }
                    else -> {
                        // this is just a sample plugin implementation
                        // normaly we at CrowdWare want to get rid of HTML in general
                        // so help yourself and extend this plugin if you really need HTML
                        println("Unknown node: ${child.name}")
                    }
                }
            }
        }
        outputFile.writeText(content)
        outputFiles.add(outputFile)
    }

    return ExportStatus(true, "Generated EPUB", outputFiles)
}
}