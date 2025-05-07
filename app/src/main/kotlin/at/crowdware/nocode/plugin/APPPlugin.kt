package at.crowdware.nocode.plugin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocode.utils.*
import java.io.File

class APPPlugin : NoCodePlugin, AppEditorPlugin {

    override val id: String = "app-plugin"
    override val label: String = "App Editor"
    override val icon: String? = "icon.svg"

    @Composable
    override fun editor(
        source: File,
        node: SmlNode,
        onChange: (SmlNode) -> Unit,
        accentColor: Color
    ) {
        var name by remember { mutableStateOf(TextFieldValue(getStringValue(node, "name", ""))) }
        var id by remember { mutableStateOf(TextFieldValue(getStringValue(node, "id", ""))) }
        var version by remember { mutableStateOf(TextFieldValue(getStringValue(node, "version", ""))) }
        var description by remember { mutableStateOf(TextFieldValue(getStringValue(node, "description", ""))) }
        var author by remember { mutableStateOf(TextFieldValue(getStringValue(node, "author", ""))) }
        
        fun save() {
            val updated = SmlNode(
                name = node.name,
                properties = mapOf(
                    "name" to PropertyValue.StringValue(name.text),
                    "id" to PropertyValue.StringValue(id.text),
                    "version" to PropertyValue.StringValue(version.text),
                    "description" to PropertyValue.StringValue(description.text),
                    "author" to PropertyValue.StringValue(author.text)
                ),
                children = node.children
            )
            onChange(updated)
            updated.saveToFile(source.toPath())
        }

        Column(
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.primary).padding(8.dp)) {
            BasicText(
                text = "App",
                modifier = Modifier.padding(8.dp),
                maxLines = 1,
                style = TextStyle(color = MaterialTheme.colors.onPrimary),
                overflow = TextOverflow.Ellipsis
            )
            Row(modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth().padding(8.dp)) {
                Column() {
                    Text("Id:", color = MaterialTheme.colors.onPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(text = id, onValueChange = { id = it;save() }, modifier = Modifier.width(400.dp), accentColor = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Name:", color = MaterialTheme.colors.onPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(text = name, onValueChange = { name = it;save() }, modifier = Modifier.width(400.dp), accentColor = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version:", color = MaterialTheme.colors.onPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(text = version, onValueChange = { version = it;save() }, modifier = Modifier.width(100.dp), accentColor = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Author:", color = MaterialTheme.colors.onPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(text = author, onValueChange = { author = it;save() }, modifier = Modifier.width(400.dp), accentColor = accentColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Description:", color = MaterialTheme.colors.onPrimary)
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(text = description, onValueChange = { description = it;save() }, modifier = Modifier.width(400.dp).height(300.dp), singleLine = false, accentColor)
                }
            }
        }
    }
}