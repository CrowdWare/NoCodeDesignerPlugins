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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.crowdware.coursereader.ui.AccordionList
import at.crowdware.coursereader.ui.ShowLecture
import at.crowdware.coursereader.ui.hexToColor
import at.crowdware.coursereader.ui.theme.CourseReaderTheme
import at.crowdware.coursereader.util.loadAndParseSml
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CourseReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = this
                    var page by remember { mutableStateOf("home.sml") }
                    var showAccordion by remember { mutableStateOf(false) }
                    val fileContent = remember {
                        context.assets.open("app.sml").bufferedReader().use { it.readText() }
                    }
                    val (_, _) = remember(fileContent) {
                        parseSML(fileContent)
                    }
                    val parsedData by remember {
                        mutableStateOf(loadAndParseSml(context))
                    }
                    var theme by remember { mutableStateOf(parsedData.theme.copy()) }
                    val topicList = remember(parsedData) { parsedData.topics }
                    val lang = remember(parsedData) { parsedData.lang }
                    val courseTitle = remember(parsedData) { parsedData.courseTitle }

                    ConfigureSystemBars(
                        statusBarColor = hexToColor(theme, theme.background),
                        navigationBarColor = hexToColor(theme, theme.background),
                        true
                    )

                    Column(modifier = Modifier.padding(innerPadding).background(hexToColor(theme, theme.primary))) {
                        val targetWeight = if (showAccordion) 0.382f else 0f
                        val accordionWeight by animateFloatAsState(targetValue = targetWeight, label = "accordionWeight")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icon ganz links
                            IconButton(
                                onClick = { showAccordion = !showAccordion }
                            ) {
                                Crossfade(targetState = showAccordion, label = "accordionToggle") { expanded ->
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                                        contentDescription = if (expanded) "Close Menu" else "Open Menu",
                                        tint = hexToColor(theme, theme.onBackground)
                                    )
                                }
                            }
                            if (showAccordion) {
                                Text(
                                    text = "Topics",
                                    color = hexToColor(theme, theme.onBackground),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Text(
                                text = courseTitle,
                                color = hexToColor(theme, theme.onBackground),
                                modifier = Modifier.padding(end = 12.dp)
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = { theme = toggleTheme(theme) }) {
                                Icon(
                                    imageVector = Icons.Filled.Brightness6,
                                    contentDescription = "Toggle Theme",
                                    tint = hexToColor(theme, theme.onBackground)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(hexToColor(theme, theme.surface))
                                .padding(8.dp)
                        ) {
                            if (accordionWeight > 0f) {
                                Column(modifier = Modifier
                                    .weight(accordionWeight)
                                    .fillMaxHeight()) {
                                    AccordionList(theme, items = topicList) { p -> page = p }
                                }
                            }
                            Column(modifier = Modifier
                                .weight(1f - accordionWeight)
                                .fillMaxHeight()) {
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
fun ConfigureSystemBars(
    statusBarColor: Color,
    navigationBarColor: Color,
    hideNavigationBar: Boolean = false
) {
    val view = LocalView.current
    val window = (view.context as Activity).window

    SideEffect {
        // Setze Farben
        window.statusBarColor = statusBarColor.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()

        val controller = WindowCompat.getInsetsController(window, view)

        controller.isAppearanceLightStatusBars = statusBarColor.luminance() > 0.5f
        controller.isAppearanceLightNavigationBars = navigationBarColor.luminance() > 0.5f

        // Optional Navigation Bar ausblenden
        if (hideNavigationBar) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

fun toggleTheme(theme: Theme): Theme {
    return theme.copy(
        primary = theme.onPrimary,
        onPrimary = theme.primary,
        primaryContainer = theme.onPrimaryContainer,
        onPrimaryContainer = theme.primaryContainer,
        surface = theme.onSurface,
        onSurface = theme.surface,
        secondary = theme.onSecondary,
        onSecondary = theme.secondary,
        secondaryContainer = theme.onSecondaryContainer,
        onSecondaryContainer = theme.secondaryContainer,
        tertiary = theme.onTertiary,
        onTertiary = theme.tertiary,
        tertiaryContainer = theme.onTertiaryContainer,
        onTertiaryContainer = theme.tertiaryContainer,
        outline = theme.outlineVariant,
        outlineVariant = theme.outline,
        onError = theme.onErrorContainer,
        onErrorContainer = theme.onError,
        inverseSurface = theme.inverseOnSurface,
        inverseOnSurface = theme.inverseSurface,
        background = theme.onBackground,
        onBackground = theme.background
    )
}