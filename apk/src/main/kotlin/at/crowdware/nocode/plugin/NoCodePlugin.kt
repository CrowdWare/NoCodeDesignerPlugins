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

 package at.crowdware.nocode.plugin


import at.crowdware.nocode.utils.App
import at.crowdware.nocode.utils.PartElement
import at.crowdware.nocode.utils.SmlNode
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.io.File


interface NoCodePlugin {
    val id: String              // z. B. "epub", "bootstrap", "compose"
    val label: String           // z. B. "EPUB 3", "Bootstrap 5"
    val icon: String?           // Optional: "epub.svg", als Pfad oder Ressource
}

interface ExportPlugin {
    suspend fun export(
        source: String,
        outputDir: File,
        onLog: (String) -> Unit = {}
    ): ExportStatus
}

interface AppEditorPlugin {
    @Composable
    fun editor(
        source: File,
        node: SmlNode,
        onChange: (SmlNode) -> Unit,
        accentColor: Color
    )
}