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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import at.crowdware.coursereader.Theme

data class Lecture(val label: String, val page: String)
data class AccordionEntry(val title: String, val content: MutableList<Lecture>)

@Composable
fun AccordionItem(entry: AccordionEntry, onClick: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(MaterialTheme.colorScheme.primary)
            .animateContentSize()
            .padding(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onPrimary
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                modifier = Modifier.rotate(if (expanded) 180f else 0f),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            for (item in entry.content) {
                Button(
                    onClick = {onClick(item.page)},
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccordionList(
    theme: Theme,
    items: List<AccordionEntry>,
    onItemClicked: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    var contentHeight by remember { mutableStateOf(0) }
    var containerHeight by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(hexToColor(theme, theme.surface))
            .onGloballyPositioned {
                containerHeight = it.size.height
            }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .onGloballyPositioned {
                    contentHeight = it.size.height
                }
        ) {
            items.forEach { entry ->
                AccordionItem(entry, onClick = onItemClicked)
            }
        }

        if (contentHeight > containerHeight) {
            // Scrollbar only if needed
            val proportion = scrollState.value.toFloat() / (scrollState.maxValue.toFloat().coerceAtLeast(1f))
            val scrollbarHeightPx = (containerHeight.toFloat() / contentHeight.toFloat()) * containerHeight
            val scrollbarOffset = proportion * (containerHeight - scrollbarHeightPx)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = with(LocalDensity.current) { scrollbarOffset.toDp() })
                    .width(4.dp)
                    .height(with(LocalDensity.current) { scrollbarHeightPx.toDp() })
                    .background(Color.DarkGray, shape = RoundedCornerShape(2.dp))
            )
        }
    }
}