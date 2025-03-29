/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

 package at.crowdware.nocode.utils

 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.unit.TextUnit
 import java.time.LocalDateTime
 
 data class App(
     var name: String = "",
     var description: String = "",
     var icon: String = "",
     var id: String = "",
     var smlVersion: String = "1.1",
     var author: String = "",
     var theme: ThemeElement = ThemeElement(),
     var deployment: DeploymentElement = DeploymentElement()
 )

 data class ThemeElement(
    var primary: String = "",
    var onPrimary: String = "",
    var primaryContainer: String = "",
    var onPrimaryContainer: String = "",
    var secondary: String = "",
    var onSecondary: String = "",
    var secondaryContainer: String = "",
    var onSecondaryContainer: String = "",
    var tertiary: String = "",
    var onTertiary: String = "",
    var tertiaryContainer: String = "",
    var onTertiaryContainer: String = "",
    var error: String = "",
    var errorContainer: String = "",
    var onError: String = "",
    var onErrorContainer: String = "",
    var background: String = "",
    var onBackground: String = "",
    var surface: String = "",
    var onSurface: String = "",
    var surfaceVariant: String = "",
    var onSurfaceVariant: String = "",
    var outline: String = "",
    var inverseOnSurface: String = "",
    var inverseSurface: String = "",
    var inversePrimary: String = "",
    var surfaceTint: String = "",
    var outlineVariant: String = "",
    var scrim: String = ""
)

data class DeploymentElement(
    val files: MutableList<FileElement> = mutableListOf()
)

data class FileElement(val path: String, val time: LocalDateTime)

data class PartElement (val src: String, val pdfOnly: Boolean = false)
 
 data class SmlNode(
    val name: String,
    val properties: Map<String, PropertyValue>,
    val children: List<SmlNode>
)
 
data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int)


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

sealed class PropertyValue {
    data class StringValue(val value: String) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
}

fun getStringValue(node: SmlNode, key: String, default: String): String {
    val value = node.properties[key]
    return when {
        value is PropertyValue.StringValue -> value.value
        value is PropertyValue -> {
            val type = value.javaClass.simpleName
            println("Warning: The value for '$key' is not a StringValue (found: $type). Returning default value: \"$default\"")
            default
        }
        else -> default
    }
}

fun getIntValue(node: SmlNode, key: String, default: Int): Int {
    val value = node.properties[key]
    return when {
        value is PropertyValue.IntValue -> value.value
        value is PropertyValue -> {
            val type = value.javaClass.simpleName
            println("Warning: The value for '$key' is not an IntValue (found: $type). Returning default value: $default")
            default
        }
        else -> default
    }
}

fun getFontWeight(node: SmlNode): FontWeight {
    val key = getStringValue(node, "fontWeight", "").trim()
    return fontWeightMap.getOrDefault(key, FontWeight.Normal)
}
fun getTextAlign(node: SmlNode): TextAlign {
    val key = getStringValue(node, "textAlign", "").trim()
    return textAlignMap.getOrDefault(key, TextAlign.Start)
}

fun getPadding(node: SmlNode): Padding {
    val paddingString = getStringValue(node, "padding", "0")
    val paddingValues = paddingString.split(" ").mapNotNull { it.toIntOrNull() }

    return when (paddingValues.size) {
        1 -> Padding(paddingValues[0], paddingValues[0], paddingValues[0], paddingValues[0]) // All sides the same
        2 -> Padding(paddingValues[0], paddingValues[1], paddingValues[0], paddingValues[1]) // Vertical and Horizontal same
        4 -> Padding(paddingValues[0], paddingValues[1], paddingValues[2], paddingValues[3]) // Top, Right, Bottom, Left
        else -> Padding(0, 0, 0, 0) // Default fallback
    }
}

 
 
 