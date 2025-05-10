/****************************************************************************
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of CourseReader.
 *
 *  CourseReader is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CourseReader is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CourseReader.  If not, see <http://www.gnu.org/licenses/>.
 *
 ****************************************************************************/

package at.crowdware.coursereader

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple7
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class PropertyValue {
    data class StringValue(val value: String) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
    data class BooleanValue(val value: Boolean) : PropertyValue()
}

val identifier: Token = regexToken("[a-zA-Z_][a-zA-Z0-9_]*")
val lBrace: Token = literalToken("{")
val rBrace: Token = literalToken("}")
val colon: Token = literalToken(":")
val stringLiteral: Token = regexToken("\"[^\"]*\"")
val whitespace: Token = regexToken("\\s+")
val integerLiteral: Token = regexToken("\\d+")
val floatLiteral = regexToken("\\d+\\.\\d+")
val booleanLiteral: Token = regexToken("true|false")

val lineComment: Token = regexToken("//.*")
val blockComment: Token = regexToken(Regex("/\\*[\\s\\S]*?\\*/", RegexOption.DOT_MATCHES_ALL))

object SmlGrammar : Grammar<List<Any>>() {
    //val whitespaceParser = zeroOrMore(whitespace)
    val commentParser = lineComment or blockComment
    val ignoredParser = zeroOrMore(whitespace or commentParser)
    val stringParser = stringLiteral.map { PropertyValue.StringValue(it.text.removeSurrounding("\"")) }
    val integerParser = integerLiteral.map { PropertyValue.IntValue(it.text.toInt()) }
    val floatParser = floatLiteral.map { PropertyValue.FloatValue(it.text.toFloat()) }
    val booleanParser = booleanLiteral.map { PropertyValue.BooleanValue(it.text.toBoolean()) }
    val propertyValue = floatParser or integerParser or booleanParser or stringParser
    val property by (ignoredParser and identifier and ignoredParser and colon and ignoredParser and propertyValue).map { (_, id, _, _, _, value) ->
        id.text to value
    }
    val elementContent: Parser<List<Any>> = zeroOrMore(property or parser { element })
    val element: Parser<Any> by ignoredParser and identifier and ignoredParser and lBrace and elementContent and ignoredParser and rBrace

    override val tokens: List<Token> = listOf(
        booleanLiteral, identifier, lBrace, rBrace, colon, stringLiteral, floatLiteral, integerLiteral,
        whitespace, lineComment, blockComment
    )
    override val rootParser: Parser<List<Any>> = (oneOrMore(element) and ignoredParser).map { (elements, _) -> elements }
}


fun extractProperties(element: Any): Map<String, PropertyValue> {
    if (element is Tuple7<*, *, *, *, *, *, *>) {
        return (element.t5 as? List<*>)?.filterIsInstance<Pair<String, PropertyValue>>()?.toMap() ?: emptyMap()
    }
    return emptyMap()
}

fun extractChildElements(element: Any): List<Any> {
    if (element is Tuple7<*, *, *, *, *, *, *>) {
        return (element.t5 as? List<*>)?.filterIsInstance<Tuple7<*, *, *, *, *, *, *>>() ?: emptyList()
    }
    return emptyList()
}

fun convertTupleToSmlNode(tuple: Any): SmlNode? {
    if (tuple !is Tuple7<*, *, *, *, *, *, *>) return null

    val nameToken = tuple.t2
    val content = tuple.t5 as? List<*> ?: return null

    val name = (nameToken as? TokenMatch)?.text ?: return null

    val properties = mutableMapOf<String, PropertyValue>()
    val children = mutableListOf<SmlNode>()

    content.forEach {
        when (it) {
            is Pair<*, *> -> {
                val key = it.first as? String ?: return@forEach
                val value = it.second as? PropertyValue ?: return@forEach
                properties[key] = value
            }
            is Tuple7<*, *, *, *, *, *, *> -> {
                convertTupleToSmlNode(it)?.let { node -> children.add(node) }
            }
        }
    }

    return SmlNode(name, properties, children)
}

fun String.lineWrap(maxLen: Int): String =
    this.chunked(maxLen).joinToString("\n")

fun parseSML(sml: String): Pair<SmlNode?, String?> {
    val rootList = try {
        SmlGrammar.parseToEnd(sml)
    } catch (e: Exception) {
        return null to "ParseError: ${e.message?.lineWrap(100)}"
    }
    return rootList.firstOrNull()?.let { convertTupleToSmlNode(it) } to null
}

data class SmlNode(
    val name: String,
    val properties: Map<String, PropertyValue>,
    val children: List<SmlNode>
)

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

fun getFloatValue(node: SmlNode, key: String, default: Float): Float {
    val value = node.properties[key]
    return when {
        value is PropertyValue.FloatValue -> value.value
        value is PropertyValue -> {
            val type = value.javaClass.simpleName
            println("Warning: The value for '$key' is not a FloatValue (found: $type). Returning default: $default")
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
fun getBoolValue(node: SmlNode, key: String, default: Boolean): Boolean {
    val value = node.properties[key]
    return when {
        value is PropertyValue.BooleanValue -> value.value
        value is PropertyValue -> {
            val type = value.javaClass.simpleName
            println("Warning: The value for '$key' is not a BooleanValue (found: $type). Returning default value: $default")
            default
        }
        else -> default
    }
}

data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int)

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

data class Theme(
    var primary: String = "",
    var onPrimary: String = "",
    var primaryContainer: String = "",
    var onPrimaryContainer: String = "",
    var surface: String = "",
    var onSurface: String = "",
    var secondary: String = "",
    var onSecondary: String = "",
    var secondaryContainer: String = "",
    var onSecondaryContainer: String = "",
    var tertiary: String = "",
    var onTertiary: String = "",
    var tertiaryContainer: String = "",
    var onTertiaryContainer: String = "",
    var outline: String = "",
    var outlineVariant: String = "",
    var onErrorContainer: String = "",
    var onError: String = "",
    var inverseSurface: String = "",
    var inversePrimary: String = "",
    var inverseOnSurface: String = "",
    var background: String = "",
    var onBackground: String = "",
    var error: String = "",
    var scrim: String = ""
)