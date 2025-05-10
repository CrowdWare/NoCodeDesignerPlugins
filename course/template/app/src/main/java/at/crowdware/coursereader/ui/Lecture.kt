/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of Course.
 *
 *  Course is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Course is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Course.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.coursereader.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.coursereader.SmlNode
import at.crowdware.coursereader.Theme
import at.crowdware.coursereader.getIntValue
import at.crowdware.coursereader.getPadding
import at.crowdware.coursereader.getStringValue
import at.crowdware.coursereader.parseSML
import java.lang.Exception


@Composable
fun ShowLecture(context: Context, theme: Theme, page: String, lang: String) {
    if (page.isEmpty()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Bitte wähle links eine Lektion aus der Liste aus.",
                color = hexToColor(theme, theme.onSurface)
            )
        }
        return
    }

    var content: String? = null
    try {
        val inputStream = context.assets.open("pages/$page")
        content = inputStream?.bufferedReader()?.use { it.readText() }
    } catch (e: Exception) {
        println("An error occured: ${e.message}")
    }
    if (content != null) {
        val (parsedPage, _) = parseSML(content)
        if (parsedPage != null) {
            val padding = getPadding(parsedPage)
            Column(
                modifier = Modifier.background(hexToColor(theme, theme.background))
                    .fillMaxSize().padding(
                        top = padding.top.dp,
                        bottom = padding.bottom.dp,
                        start = padding.left.dp,
                        end = padding.right.dp
                    )
            ) {

                for (element in parsedPage.children) {
                    renderElement(context, theme, element, lang)
                }
            }
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Beim Laden der Datei $page ist ein Fehler aufgetreten.",
                color = hexToColor(theme, theme.onSurface)
            )
        }
    }
}

@Composable
fun renderElement(context: Context, theme: Theme, node: SmlNode, lang: String) {
    when (node.name) {
        "Column" -> {
            renderColumn(context, theme, node, lang)
        }
        "Row" -> {
            renderRow(context, theme, node, lang)
        }
        "Markdown" -> {
            renderMarkdown(context, modifier = Modifier, theme, node, lang)
        }
        "Text" -> {
            renderText(theme, node)
        }
        "Image" -> {
            renderImage(theme, node)
        }
        "Youtube" -> {
            renderYoutube(theme, node)
        }
        "Button" -> {
            renderButton(theme, node)
        }
        else -> {
            println("unhandled element: ${node.name}")
        }
    }
}

@Composable
fun renderColumn(context: Context, theme: Theme, node: SmlNode, lang: String) {
    val padding = getPadding(node)
    Column(modifier = Modifier.padding(top = padding.top.dp, bottom = padding.bottom.dp, start = padding.left.dp, end = padding.right.dp)) {
        for (n in node.children) {
            renderElement(context, theme, n, lang)
        }
    }
}

@Composable
fun renderRow(context: Context, theme: Theme, node: SmlNode, lang: String) {
    val padding = getPadding(node)
    Row(modifier = Modifier.padding(top = padding.top.dp, bottom = padding.bottom.dp, start = padding.left.dp, end = padding.right.dp)) {
        for (n in node.children) {
            renderElement(context, theme, n, lang)
        }
    }
}

@Composable
fun renderMarkdown(context: Context, modifier: Modifier = Modifier, theme: Theme, node: SmlNode, lang: String) {
    val text = getStringValue(node, "text", "")
    val color = getStringValue(node, "color", "onBackground")
    val fontSize = getIntValue(node, "fontSize", 16)

    var txt = text
    if (text.startsWith("part:")) {
        val part = text.substringAfter("part:")
        val inputStream = context.assets.open("parts/$part-$lang.md")
        val content = inputStream?.bufferedReader()?.use { it.readText() }
        if (content != null) {
            txt = content
        }
    }
    val parsedMarkdown = parseMarkdown(txt)
    Text(
        modifier = modifier.fillMaxWidth(),
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(theme, color)),
        fontSize = fontSize.sp,
        fontWeight = getFontWeight(node),
        textAlign = getTextAlign(node)
    )
}

@Composable
fun renderText(theme: Theme, node: SmlNode) {
    val text = getStringValue(node, "text", "")
    Text(text)
}

@Composable
fun renderImage(theme: Theme, node: SmlNode) {

}

@Composable
fun renderYoutube(theme: Theme, node: SmlNode) {

}

@Composable
fun renderButton(theme: Theme, node: SmlNode) {

}

@Composable
fun parseMarkdown(markdown: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val lines = markdown.split("\n") // Process each line individually

    for (i in lines.indices) {
        val line = lines[i]
        var j = 0
        var inCodeBlock = false

        while (j < line.length) {
            if (line[j] == '`') {
                inCodeBlock = !inCodeBlock
                j++
                continue
            }

            if (inCodeBlock) {
                // Append text literally when in code mode
                val endOfCodeBlock = line.indexOf("`", j)
                if (endOfCodeBlock != -1) {
                    builder.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(line.substring(j, endOfCodeBlock))
                    }
                    j = endOfCodeBlock + 1
                    inCodeBlock = false // Close code mode
                } else {
                    // If no closing backtick is found, append till end of line
                    builder.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(line.substring(j))
                    }
                    j = line.length
                }
                continue
            }
            when {
                line.startsWith("###### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("###### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("##### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("##### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("#### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("#### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("## ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("## ").trim())
                    }
                    j = line.length
                }
                line.startsWith("# ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("# ").trim())
                    }
                    j = line.length
                }
                line.startsWith("![", j) -> {
                    // ignore images here
                    val endParen = line.indexOf(")", j)
                    if(endParen == -1)  // not found
                        j++
                    else
                        j = endParen + 1
                }
                line.startsWith("[", j) -> {

                    val endBracket = line.indexOf("]", j)
                    val startParen = line.indexOf("(", endBracket)
                    val endParen = line.indexOf(")", startParen)

                    if (endBracket != -1 && startParen == endBracket + 1 && endParen != -1) {
                        val linkText = line.substring(j + 1, endBracket)
                        val linkUrl = line.substring(startParen + 1, endParen)

                        builder.pushStringAnnotation(tag = "URL", annotation = linkUrl)
                        builder.withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(linkText)
                        }
                        builder.pop()
                        j = endParen + 1
                    } else {
                        builder.append(line[j])
                        j++
                    }
                }
                line.startsWith("<", j) && line.indexOf(">", j) > j -> {
                    // ignore html tags
                    val endParen = line.indexOf(">", j)
                    j = endParen + 1
                }
                line.startsWith("***", j) -> {
                    val endIndex = line.indexOf("***", j + 3)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 3, endIndex).trim())
                        }
                        j = endIndex + 3
                    } else {
                        builder.append("***")
                        j += 3
                    }
                }
                line.startsWith("**", j) -> {
                    val endIndex = line.indexOf("**", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("**")
                        j += 2
                    }
                }
                line.startsWith("*", j) -> {
                    val endIndex = line.indexOf("*", j + 1)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 1, endIndex).trim())
                        }
                        j = endIndex + 1
                    } else {
                        builder.append("*")
                        j += 1
                    }
                }
                line.startsWith("~~", j) -> {
                    val endIndex = line.indexOf("~~", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("~~")
                        j += 2
                    }
                }
                line.startsWith("(c)", j, ignoreCase = true) -> {
                    builder.append("©")
                    j += 3
                }
                line.startsWith("(r)", j, ignoreCase = true) -> {
                    builder.append("®")
                    j += 3
                }
                line.startsWith("(tm)", j, ignoreCase = true) -> {
                    builder.append("™")
                    j += 4
                }
                else -> {
                    builder.append(line[j])
                    j++
                }
            }
        }

        if (i < lines.size - 1) {
            builder.append("\n")
        }
    }

    return builder.toAnnotatedString()
}

fun hexToColor(theme: Theme, hex: String, default: String = "#000000"): Color {
    var value = hex
    if (hex.isEmpty()) {
        value = default
    }

    if(!hex.startsWith("#")) {
        when(hex) {
            "primary" -> {value = theme.primary }
            "onPrimary" -> {value = theme.onPrimary }
            "primaryContainer" -> {value = theme.primaryContainer }
            "onPrimaryContainer" -> {value =theme.onPrimaryContainer }
            "surface" -> {value = theme.surface }
            "onSurface" -> {value = theme.onSurface }
            "secondary" -> {value = theme.secondary }
            "onSecondary" -> {value = theme.onSecondary }
            "secondaryContainer" -> {value = theme.secondaryContainer }
            "onSecondaryContainer" -> {value = theme.onSecondaryContainer }
            "tertiary" -> {value = theme.tertiary }
            "onTertiary" -> {value = theme.onTertiary }
            "tertiaryContainer" -> {value = theme.tertiaryContainer }
            "onTertiaryContainer" -> {value = theme.onTertiaryContainer }
            "outline" -> {value = theme.outline }
            "outlineVariant" -> {value = theme.outlineVariant }
            "onErrorContainer" -> {value = theme.onErrorContainer }
            "onError" -> {value = theme.onError }
            "inverseSurface" -> {value = theme.inverseSurface }
            "inversePrimary" -> {value = theme.inversePrimary }
            "inverseOnSurface" -> {value = theme.inverseOnSurface }
            "background" -> {value = theme.background }
            "onBackground" -> {value = theme.onBackground }
            "error" -> {value = theme.error }
            "scrim" -> {value = theme.scrim }
            else -> {value = default}
        }
    }

    val color = value.trimStart('#')
    return when (color.length) {
        6 -> {
            // Hex without alpha (e.g., "RRGGBB")
            val r = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b)
        }
        8 -> {
            // Hex with alpha (e.g., "AARRGGBB")
            val a = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val r = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(6, 8).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b, a)
        }
        else -> Color.Black
    }
}

fun getFontWeight(node: SmlNode): FontWeight {
    val key = getStringValue(node, "fontWeight", "").trim().lowercase()
    return fontWeightMap.getOrDefault(key, FontWeight.Normal)
}

fun getTextAlign(node: SmlNode): TextAlign {
    val key = getStringValue(node, "textAlign", "").trim().lowercase()
    return textAlignMap.getOrDefault(key, TextAlign.Start)
}

val fontWeightMap = mapOf(
    "bold" to FontWeight.Bold,
    "black" to FontWeight.Black,
    "thin" to FontWeight.Thin,
    "extrabold" to FontWeight.ExtraBold,
    "extralight" to FontWeight.ExtraLight,
    "light" to FontWeight.Light,
    "medium" to FontWeight.Medium,
    "semibold" to FontWeight.SemiBold,
    "" to FontWeight.Normal
)

val textAlignMap = mapOf(
    "left" to TextAlign.Start,
    "center" to TextAlign.Center,
    "right" to TextAlign.End,
    "" to TextAlign.Start
)