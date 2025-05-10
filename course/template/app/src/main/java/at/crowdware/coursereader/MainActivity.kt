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

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import at.crowdware.coursereader.ui.AccordionEntry
import at.crowdware.coursereader.ui.AccordionList
import at.crowdware.coursereader.ui.Lecture
import at.crowdware.coursereader.ui.ShowLecture
import at.crowdware.coursereader.ui.hexToColor
import at.crowdware.coursereader.ui.theme.CourseReaderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CourseReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = this
                    var lang by remember { mutableStateOf("") }
                    var page by remember { mutableStateOf("") }
                    val topicList = mutableListOf<AccordionEntry>()
                    var showAccordion by remember { mutableStateOf(true) }
                    val theme = Theme()
                    val inputStream = this.assets.open("app.sml")
                    val fileContent = inputStream.bufferedReader().use { it.readText() }
                    val (parsedApp, _) = parseSML(fileContent)
                    if (parsedApp != null) {
                        for (node in parsedApp.children) {
                            if (node.name == "Theme") {
                                theme.primary = getStringValue(node, "primary", "")
                                theme.onPrimary = getStringValue(node, "onPrimary", "")
                                theme.primaryContainer = getStringValue(node, "primaryContainer", "")
                                theme.onPrimaryContainer = getStringValue(node, "onPrimaryContainer", "")
                                theme.surface = getStringValue(node, "surface", "")
                                theme.onSurface = getStringValue(node, "onSurface", "")
                                theme.secondary = getStringValue(node, "secondary", "")
                                theme.onSecondary = getStringValue(node, "onSecondary", "")
                                theme.secondaryContainer = getStringValue(node, "secondaryContainer", "")
                                theme.onSecondaryContainer = getStringValue(node, "onSecondaryContainer", "")
                                theme.tertiary = getStringValue(node, "tertiary", "")
                                theme.onTertiary = getStringValue(node, "onTertiary", "")
                                theme.tertiaryContainer = getStringValue(node, "tertiaryContainer", "")
                                theme.onTertiaryContainer = getStringValue(node, "onTertiaryContainer", "")
                                theme.outline = getStringValue(node, "outline", "")
                                theme.outlineVariant = getStringValue(node, "outlineVariant", "")
                                theme.onErrorContainer = getStringValue(node, "onErrorContainer", "")
                                theme.onError = getStringValue(node, "onError", "")
                                theme.inverseSurface = getStringValue(node, "inverseSurface", "")
                                theme.inversePrimary = getStringValue(node, "inversePrimary", "")
                                theme.inverseOnSurface = getStringValue(node, "inverseOnSurface", "")
                                theme.background = getStringValue(node, "background", "")
                                theme.onBackground = getStringValue(node, "onBackground", "")
                                theme.error = getStringValue(node, "error", "")
                                theme.scrim = getStringValue(node, "scrim", "")
                                SetSystemBarsColor(
                                    statusBarColor = hexToColor(theme, theme.background),
                                    navigationBarColor = hexToColor(theme, theme.background)
                                )
                            }
                            else if (node.name == "Course") {
                                lang = getStringValue(node, "lang", "")
                                for (topic in node.children) {
                                    if (topic.name == "Topic") {
                                        val entries = mutableListOf<Lecture>()
                                        for (lecture in topic.children) {
                                            entries.add(
                                                Lecture(
                                                    label = getStringValue(lecture, "label", ""),
                                                    page = getStringValue(lecture, "src", "")
                                                )
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
                    Column(modifier = Modifier.padding(innerPadding).background(hexToColor(theme, theme.background))) {
                        // Header mit Toggle-Button
                        Row(modifier = Modifier.height(35.dp).padding(8.dp)) {
                            Text(
                                text = "Topics",
                                color = hexToColor(theme, theme.onBackground),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showAccordion = !showAccordion }) {
                                Icon(
                                    imageVector = if (showAccordion) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                                    contentDescription = "Toggle Accordion",
                                    tint = hexToColor(theme, theme.onBackground)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.background(hexToColor(theme, theme.surface))
                                .fillMaxHeight().padding(8.dp)
                        ) {
                            AnimatedVisibility(visible = showAccordion) {
                                Column(modifier = Modifier.width(200.dp)) {
                                    AccordionList(theme, items = topicList) { p -> page = p }
                                }
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ShowLecture(context, theme, page, lang)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetSystemBarsColor(statusBarColor: Color, navigationBarColor: Color) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    SideEffect {
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()

        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = statusBarColor.luminance() > 0.5f
        insetsController.isAppearanceLightNavigationBars = navigationBarColor.luminance() > 0.5f
    }
}


