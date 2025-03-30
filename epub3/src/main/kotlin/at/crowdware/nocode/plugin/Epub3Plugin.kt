/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of NoCodeDesigner.
 *
 *  NoCodeDesigner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeDesigner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeDesigner.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocode.plugin


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

class Epub3Plugin : SmlExportPlugin {
    override val id = "epub3-plugin"
    override val label = "Epub 3 Generator"
    override val icon = "icon.svg"

    override fun export(source: String, outputDir: File): ExportStatus {
        val generator = "NoCodeDesigner + epub3 (plugin)"
        val outputFiles = mutableListOf<File>()
        val appFile = File(source, "app.sml")
        val appSml = appFile.readText()
        val (parsedApp, _) = parseSML(appSml)
        val title = getStringValue(parsedApp!!, "title", "defaultTitle")
        val languages = listOf("de", "en", "pt", "fr", "eo", "es")
        for (lang in languages) {
            val tempDir = createTempDirectory().toFile()
            val guid = UUID.randomUUID().toString()

            File(tempDir, "EPUB/parts").mkdirs()
            File(tempDir, "EPUB/images").mkdirs()
            File(tempDir, "EPUB/css").mkdirs()
            File(tempDir, "META-INF").mkdirs()

            copyAssets(tempDir)
            copyImages(tempDir, source)
            writeContainer(tempDir)
            writeMimetype(tempDir)
            generatePackage(tempDir, parsedApp, source, guid, lang, generator)
            val (toc, partsCount) = generateParts(tempDir, source, lang = lang)
            if (partsCount > 0) {
                generateToc(tempDir, parsedApp, toc, lang, generator)

                val files = getAllFiles(tempDir)
                val out = "$outputDir/$title-$lang.epub"
                ZipOutputStream(Files.newOutputStream(Paths.get(out))).use { zip ->
                    files.forEach { file ->
                        zip.putNextEntry(ZipEntry(file.relativeTo(tempDir).path))
                        zip.write(file.readBytes())
                        zip.closeEntry()
                    }
                }
                tempDir.deleteRecursively()
                outputFiles.add(File(out))
            }
        }
        return ExportStatus(true, "generated ${outputFiles.size} EPUB files", outputFiles)
    }

    fun getAllFiles(dir: File): List<File> {
        return dir.walk().filter { it.isFile }.toList()
    }

    fun copyAssets(targetDir: File) {
        val assetFiles = listOf(
            "assets/css/pastie.css",
            "assets/css/stylesheet.css"
        )

        for (filePath in assetFiles) {
            // Verwende den ClassLoader, um die Datei aus dem JAR zu laden
            val inputStream = javaClass.classLoader.getResourceAsStream(filePath)
                ?: throw IllegalArgumentException("Resource not found: $filePath")

            // Erstelle das Zielverzeichnis und die Zieldatei
            val targetFile = File(targetDir, "EPUB/" + filePath.removePrefix("assets/"))
            targetFile.parentFile?.mkdirs()

            // Kopiere die Datei aus dem JAR in das Zielverzeichnis
            inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun copyStreamToFile(inputStream: InputStream, targetFile: File) {
        targetFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    fun copyImages(dir: File, source: String) {
        val sourceDir = File(source, "images")
        val targetDir = File(dir, "EPUB/images")
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

    fun writeMimetype(dir: File) {
        val mimeFile = File(dir, "mimetype")
        mimeFile.writeText("application/epub+zip", Charsets.UTF_8)
    }

    fun writeContainer(dir: File) {
        val metaInfDir = File(dir, "META-INF")

        metaInfDir.mkdirs()

        val containerFile = File(metaInfDir, "container.xml")
        containerFile.writeText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<container xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\" version=\"1.0\">\n" +
                "  <rootfiles>\n" +
                "    <rootfile full-path=\"EPUB/package.opf\" media-type=\"application/oebps-package+xml\"/>\n" +
                "  </rootfiles>\n" +
                "</container>", Charsets.UTF_8)
    }

    fun generatePackage(dir: File, node: SmlNode, source: String, guid: String, lang: String, generator: String) {
        val context = mutableMapOf<String, Any>()

        context["uuid"] = guid
        context["lang"] = lang
        context["title"] = getStringValue(node, "title", "defaultVersion")
        context["date"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        context["version"] = getStringValue(node, "version", "defaultVersion")
        context["creator"] = getStringValue(node, "creator", "defaultCreator")
        context["creatorLink"] = getStringValue(node, "creatorLink", "defaultCreatorLink")
        context["bookLink"] = getStringValue(node, "bookLink", "defaultLink")
        context["license"] = getStringValue(node, "license", "defaultLicense")
        context["licenseLink"] = getStringValue(node, "licenseLink", "defaultLicenseLink")
        context["generator"] = generator
        context["license"] = "GPL-3 license"

        val items = mutableListOf<Map<String, String>>()
        val spine = mutableListOf<String>()

        val partDir = File(source, "parts-$lang")
        partDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val name = file.name.substringBefore(".").replace(" ", "-").lowercase()
                if (name != "toc") {
                    val item = mutableMapOf<String, String>()
                    item["href"] = "parts/$name.xhtml"
                    item["id"] = name
                    item["type"] = "application/xhtml+xml"
                    items.add(item)
                    spine.add(name)
                }
            }
        }

        val imagesDir = File(dir, "EPUB/images")
        if (imagesDir.exists()) {
            imagesDir.walkTopDown().forEach { file ->
                if (file.isFile && file.name != ".DS_Store") {
                    val filename = file.nameWithoutExtension
                    val extension = file.extension
                    val item = mutableMapOf<String, String>()
                    item["href"] = "images/${file.name}"
                    item["id"] = "${filename}_img"
                    item["type"] = "image/$extension"
                    items.add(item)
                }
            }
        }
        // Add items and spine to context
        context["items"] = items
        context["spine"] = spine

        // Read and process the template file
        val packageName = "layout/package-$lang.opf"
        val inputStream = javaClass.classLoader.getResourceAsStream(packageName)
            ?: throw IllegalArgumentException("Layout not found: $packageName")
        val data = inputStream.bufferedReader().use { it.readText() } ?: throw IllegalArgumentException("File not found: $packageName")

        val template = Template.parse(data)
        val renderedXml = template.processToString(context)

        // Write the rendered XML to the output file
        val outputPath = Paths.get(dir.path, "EPUB", "package.opf")
        outputPath.parent.createDirectories()
        File(outputPath.toUri()).writeText(renderedXml, Charsets.UTF_8)
    }

    fun generateParts(dir: File, source: String, lang: String): Pair<List<Map<String, Any>>, Int> {
        var partsCount = 0
        val toc = mutableListOf<Map<String, Any>>()
        val item = mutableMapOf<String, Any>(
            "href" to "toc.xhtml",
            "name" to if (lang == "de") "Inhaltsverzeichnis" else "Table of Contents",  // TODO: more languages to support
            "id" to "nav",
            "parts" to mutableListOf<Any>()
        )
        toc.add(item)

        val partDir = File(source, "parts-$lang")
        partDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                partsCount ++
                println("generate part: ${file.absolutePath}")
                val context = mutableMapOf<String, Any>()
                val text = file.readText(Charsets.UTF_8)
                val name = file.name.substringBefore(".").replace(" ", "-").lowercase()

                if (name != "toc") {
                    val options = MutableDataSet()
                    options.set(HtmlRenderer.GENERATE_HEADER_ID, true)
                    options.set(HtmlRenderer.RENDER_HEADER_ID, true)
                    // Tabellenunterstützung hinzufügen
                    options.set(Parser.EXTENSIONS, listOf(TablesExtension.create()))

                    val parser = Parser.builder(options).build()
                    val document = parser.parse(text)
                    val renderer = HtmlRenderer.builder(options).build()
                    // Markdown processing and table fixing
                    var html = fixImagePaths(fixTables(renderer.render(document)))

                    val linkList = getLinks(html, name)
                    toc.addAll(linkList)

                    context["content"] = html

                    val classLoader = Thread.currentThread().contextClassLoader
                    val resourcePath = "layout/template.xhtml"
                    println("resPath: $resourcePath")
                    val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
                        ?: throw IllegalArgumentException("Resource not found: $resourcePath")

                    val templateData = inputStream.bufferedReader().use { it.readText() }
                        ?: throw IllegalArgumentException("File not found: $resourcePath")

                    val template = Template.parse(templateData)
                    val xhtml = template.processToString(context)

                    val outputFile = Paths.get(dir.path, "EPUB", "parts", "$name.xhtml").toFile()
                    outputFile.writeText(xhtml, Charsets.UTF_8)
                }
            }
        }
        return Pair(toc, partsCount)
    }

    private fun fixImagePaths(input: String): String {
        return input.replace("src=\"", "src=\"../images/")
    }

    private fun getLinks(text: String, partName: String): List<Map<String, Any>> {
        val nodes = mutableListOf<Map<String, Any>>()
        val linksList = mutableListOf<Map<String, Any>>()

        for (line in text.split("\n")) {
            if (line.isBlank()) continue

            val c = when {
                line.startsWith("<h1 ") -> 1
                line.startsWith("<h2 ") -> 2
                line.startsWith("<h3 ") -> 3
                line.startsWith("<h4 ") -> 4
                line.startsWith("<h5 ") -> 5
                line.startsWith("<h6 ") -> 6
                else -> 0
            }

            if (c > 0) {
                val idStart = line.indexOf("id=") + 4
                val idEnd = line.indexOf('"', idStart)
                val id = line.substring(idStart, idEnd)

                val nameStart = line.indexOf(">", idEnd) + 1
                val nameEnd = line.indexOf("<", nameStart)
                val name = line.substring(nameStart, nameEnd)

                val item = mutableMapOf<String, Any>()
                item["href"] = "$partName.xhtml#$id"
                item["name"] = name
                item["id"] = id
                item["hasparts"] = false
                item["parts"] = mutableListOf<Map<String, Any>>()

                if (nodes.size < c) {
                    nodes.add(item)
                } else {
                    nodes[c - 1] = item
                }

                if (c == 1) {
                    linksList.add(item)
                } else {
                    (nodes[c - 2] as MutableMap<String, Any>)["hasparts"] = true
                    (nodes[c - 2]["parts"] as MutableList<Map<String, Any>>).add(item)
                }
            }
        }
        return linksList
    }

    fun fixTables(text: String): String {
        return text
            .replace("<th align=\"center\"", "<th class=\"center\"")
            .replace("<th align=\"right\"", "<th class=\"right\"")
            .replace("<th align=\"left\"", "<th class=\"left\"")
            .replace("<td align=\"center\"", "<td class=\"center\"")
            .replace("<td align=\"right\"", "<td class=\"right\"")
            .replace("<td align=\"left\"", "<td class=\"left\"")
    }

    fun generateToc(dir: File, node: SmlNode, parts: List<Map<String, Any>>, lang: String, generator: String) {
        val context = mutableMapOf<String, Any>()

        context["lang"] = lang
        context["title"] = getStringValue(node, "title", "defaultTitle")
        context["date"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
        context["version"] = getStringValue(node, "version", "defaultVersion")
        context["creator"] = getStringValue(node, "creator", "defaultCreator")
        context["creatorLink"] = getStringValue(node, "creatorLink", "defaultCreatorLink")
        context["bookLink"] = getStringValue(node, "bookLink", "defaultBookLink")
        context["generator"] = generator
        context["publisher"] = "CrowdWare"
        if (lang == "de") {
            context["publishedby"] = "Publiziert von"
            context["licenseInformation"] = "Lizenzinformationen"
            context["from"] = "von"
            context["softwareLicense"] = "Software Lizenz"
            context["licenseTextA"] = "Dieses Buch wurde mit der"
            context["isLicensedUnder"] = "ist lizenziert unter einer nicht-kommerziellen Lizenz."
            context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
            context["licenseTextB"] = "Open Source Version"
            context["licenseTextC"] = "des"
            context["licenseTextD"] = " erstellt."
        } else if (lang == "pt") {
            context["publishedby"] = "Publicado por"
            context["licenseInformation"] = "Informações sobre a licença"
            context["from"] = "de"
            context["softwareLicense"] = "Licença de Software"
            context["licenseTextA"] = "Este livro foi criado com a"
            context["isLicensedUnder"] = "está licenciado sob uma licença não comercial."
            context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
            context["licenseTextB"] = "versão open source"
            context["licenseTextC"] = "do"
            context["licenseTextD"] = "."
        } else if (lang == "fr") {
            context["publishedby"] = "Publié par"
            context["licenseInformation"] = "Informations sur la licence"
            context["from"] = "de"
            context["softwareLicense"] = "Licence de logiciel"
            context["licenseTextA"] = "Ce livre a été créé avec la"
            context["isLicensedUnder"] = "est sous une licence non commerciale."
            context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
            context["licenseTextB"] = "version open source"
            context["licenseTextC"] = "du"
            context["licenseTextD"] = "."
        } else if (lang == "eo") {
            context["publishedby"] = "Eldoneita de"
            context["licenseInformation"] = "Licencaj informoj"
            context["from"] = "de"
            context["softwareLicense"] = "Programara Licenco"
            context["licenseTextA"] = "Ĉi tiu libro estis kreita kun la"
            context["isLicensedUnder"] = "estas licencita sub ne-komerca licenco."
            context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
            context["licenseTextB"] = "malferman fontan version"
            context["licenseTextC"] = "de la"
            context["licenseTextD"] = "."
        } else if (lang == "es") {
            context["publishedby"] = "Publicado por"
            context["licenseInformation"] = "Información de la licencia"
            context["from"] = "de"
            context["softwareLicense"] = "Licencia de software"
            context["licenseTextA"] = "Este libro ha sido creado con la"
            context["isLicensedUnder"] = "está licenciado bajo una licencia no comercial."
            context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
            context["licenseTextB"] = "versión de código abierto"
            context["licenseTextC"] = "de la"
            context["licenseTextD"] = "."
        } else {
            // Fallback auf Englisch, falls die Sprache nicht erkannt wird
            context["publishedby"] = "Published by"
            context["licenseInformation"] = "License information"
            context["from"] = "from"
            context["softwareLicense"] = "Software License"
            context["licenseTextA"] = "This book has been created with the"
            context["isLicensedUnder"] = "is licensed under a non-commercial license."
            context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
            context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
            context["licenseTextB"] = "open source version"
            context["licenseTextC"] = "of the"
            context["licenseTextD"] = "."
        }

        context["pageTitle"] = when (lang) {
            "de" -> "Inhaltsverzeichnis"
            "en" -> "Table of Contents"
            "pt" -> "Índice"
            "fr" -> "Table des matières"
            "eo" -> "Enhavo"
            "es" -> "Índice"
            else -> "Table of Contents"
        }

        if (parts.size > 0)
            context["parts"] = parts

        val resourcePath = "layout/toc.xhtml"
        val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        val templateData = inputStream.bufferedReader().use { it.readText() } ?: throw IllegalArgumentException("File not found: $resourcePath")

        val template = Template.parse(templateData)
        val xhtml = template.processToString(context)

        val outputPath = Paths.get(dir.path, "EPUB", "parts", "toc.xhtml")
        Files.writeString(outputPath, xhtml, StandardCharsets.UTF_8)
    }
}