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

package at.crowdware.coursereader.util

import android.content.Context
import at.crowdware.coursereader.Theme
import at.crowdware.coursereader.getStringValue
import at.crowdware.coursereader.parseSML
import at.crowdware.coursereader.ui.AccordionEntry
import at.crowdware.coursereader.ui.Lecture

data class ParsedCourse(
    val theme: Theme,
    val topics: List<AccordionEntry>,
    val lang: String,
    val courseTitle: String
)

fun loadAndParseSml(context: Context): ParsedCourse {
    val inputStream = context.assets.open("app.sml")
    val fileContent = inputStream.bufferedReader().use { it.readText() }
    val (parsedApp, _) = parseSML(fileContent)
    val theme = Theme()
    val topicList = mutableListOf<AccordionEntry>()
    var lang = ""
    var courseTitle = ""

    if (parsedApp != null) {
        for (node in parsedApp.children) {
            when (node.name) {
                "Theme" -> {
                    with(theme) {
                        primary = getStringValue(node, "primary", "")
                        onPrimary = getStringValue(node, "onPrimary", "")
                        primaryContainer = getStringValue(node, "primaryContainer", "")
                        onPrimaryContainer = getStringValue(node, "onPrimaryContainer", "")
                        surface = getStringValue(node, "surface", "")
                        onSurface = getStringValue(node, "onSurface", "")
                        secondary = getStringValue(node, "secondary", "")
                        onSecondary = getStringValue(node, "onSecondary", "")
                        secondaryContainer = getStringValue(node, "secondaryContainer", "")
                        onSecondaryContainer = getStringValue(node, "onSecondaryContainer", "")
                        tertiary = getStringValue(node, "tertiary", "")
                        onTertiary = getStringValue(node, "onTertiary", "")
                        tertiaryContainer = getStringValue(node, "tertiaryContainer", "")
                        onTertiaryContainer = getStringValue(node, "onTertiaryContainer", "")
                        outline = getStringValue(node, "outline", "")
                        outlineVariant = getStringValue(node, "outlineVariant", "")
                        onErrorContainer = getStringValue(node, "onErrorContainer", "")
                        onError = getStringValue(node, "onError", "")
                        inverseSurface = getStringValue(node, "inverseSurface", "")
                        inversePrimary = getStringValue(node, "inversePrimary", "")
                        inverseOnSurface = getStringValue(node, "inverseOnSurface", "")
                        background = getStringValue(node, "background", "")
                        onBackground = getStringValue(node, "onBackground", "")
                        error = getStringValue(node, "error", "")
                        scrim = getStringValue(node, "scrim", "")
                    }
                }

                "Course" -> {
                    lang = getStringValue(node, "lang", "")
                    courseTitle = getStringValue(node, "title", "")
                    for (topic in node.children) {
                        if (topic.name == "Topic") {
                            val entries = topic.children.map { lecture ->
                                Lecture(
                                    label = getStringValue(lecture, "label", ""),
                                    page = getStringValue(lecture, "src", "")
                                )
                            }
                            topicList.add(
                                AccordionEntry(
                                    getStringValue(topic, "label", ""),
                                    entries
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    return ParsedCourse(theme, topicList, lang, courseTitle)
}