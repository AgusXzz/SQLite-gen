package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.SchemaViewModel
import kotlinx.coroutines.launch

private val ContentMaxWidth = 920.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodePreviewScreen(
    viewModel: SchemaViewModel,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf("better-sqlite3") }
    val tabTitles = listOf("better-sqlite3", "node:sqlite", "sqlite3", "raw_sql")
    val code = viewModel.getGeneratedScript(selectedType)
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generated Script") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, code)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share script"))
                    }) {
                        Icon(Icons.Default.DataObject, contentDescription = "Share")
                    }
                    FilledTonalIconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Schema Script", code))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = tabTitles.indexOf(selectedType),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                tabTitles.forEach { title ->
                    Tab(
                        selected = selectedType == title,
                        onClick = { selectedType = title },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    CodeBlock(code = code)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    viewModel: SchemaViewModel,
    onBack: () -> Unit
) {
    var textOutput by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val exportJsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = viewModel.getExportedJson()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                    Toast.makeText(context, "Exported JSON successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "JSON Export Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                    val success = viewModel.importFromJson(json)
                    if (success) {
                        Toast.makeText(context, "Successfully Imported JSON", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Invalid JSON File", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val exportSqlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/sql")) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val sql = viewModel.getExportedSql()
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(sql) }
                    Toast.makeText(context, "Exported SQL successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "SQL Export Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importSqlLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val sql = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                    val success = viewModel.importFromSql(sql)
                    if (success) {
                        Toast.makeText(context, "Successfully Imported SQL", Toast.LENGTH_SHORT).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "Invalid SQL File", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import / Export") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionCard(title = "Preview Data", icon = Icons.Outlined.Code) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = { coroutineScope.launch { textOutput = viewModel.getExportedJson() } },
                            modifier = Modifier.weight(1f)
                        ) { Text("Show JSON") }
                        FilledTonalButton(
                            onClick = { coroutineScope.launch { textOutput = viewModel.getExportedSql() } },
                            modifier = Modifier.weight(1f)
                        ) { Text("Show SQL") }
                    }
                }

                SectionCard(title = "JSON File", icon = Icons.Default.DataObject) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { importJsonLauncher.launch("application/json") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Upload") }
                        Button(
                            onClick = { exportJsonLauncher.launch("schema.json") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Download") }
                    }
                }

                SectionCard(title = "SQL File", icon = Icons.Default.Storage) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { importSqlLauncher.launch("*/*") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Upload") }
                        Button(
                            onClick = { exportSqlLauncher.launch("schema.sql") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Download") }
                    }
                }

                OutlinedTextField(
                    value = textOutput,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp),
                    label = { Text("Output Preview") },
                    trailingIcon = {
                        if (textOutput.isNotEmpty()) {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Schema Preview", textOutput))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    val lines = remember(code) { code.split("\n") }
    val vScroll = rememberScrollState()
    val codeStyle = MaterialTheme.typography.bodySmall.copy(
        fontFamily = FontFamily.Monospace,
        lineHeight = 20.sp
    )
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
            .verticalScroll(vScroll)
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            lines.indices.forEach { i ->
                Text(
                    text = (i + 1).toString(),
                    style = codeStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }
        }
        SelectionContainer(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(end = 12.dp)
            ) {
                lines.forEach { line ->
                    Text(
                        text = line.ifEmpty { " " },
                        style = codeStyle,
                        softWrap = false,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
