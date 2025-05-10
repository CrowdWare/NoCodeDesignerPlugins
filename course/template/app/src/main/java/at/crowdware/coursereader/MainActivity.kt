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
import at.crowdware.coursereader.util.loadAndParseSml

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CourseReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = this
                    var page by remember { mutableStateOf("") }
                    var showAccordion by remember { mutableStateOf(true) }
                    val fileContent = remember {
                        context.assets.open("app.sml").bufferedReader().use { it.readText() }
                    }
                    val (parsedApp, _) = remember(fileContent) {
                        parseSML(fileContent)
                    }
                    val parsedData by remember {
                        mutableStateOf(loadAndParseSml(context))
                    }
                    val theme = remember(parsedData) { parsedData.theme }
                    val topicList = remember(parsedData) { parsedData.topics }
                    val lang = remember(parsedData) { parsedData.lang }
                    val courseTitle = remember(parsedData) { parsedData.courseTitle }

                    SetSystemBarsColor(
                        statusBarColor = hexToColor(theme, theme.background),
                        navigationBarColor = hexToColor(theme, theme.background)
                    )

                    Column(modifier = Modifier.padding(innerPadding).background(hexToColor(theme, theme.primary))) {
                        Row(modifier = Modifier.height(40.dp).padding(8.dp)) {
                            Text(
                                text = "Topics",
                                color = hexToColor(theme, theme.onBackground),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = courseTitle,
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


