package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.SchemaViewModel
import kotlinx.coroutines.launch

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
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Schema Script", code))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                    }
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
                edgePadding = 16.dp
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedType == title,
                        onClick = { selectedType = title },
                        text = { Text(title) }
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                SelectionContainer {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    )
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

    // JSON Launchers
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

    // SQL Launchers
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
                title = { Text("Import/Export Database") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text View Actions
            Text("Preview Data", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            textOutput = viewModel.getExportedJson()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Show JSON")
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            textOutput = viewModel.getExportedSql()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Show SQL")
                }
            }

            // File Actions JSON
            Text("JSON File Operations", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        importJsonLauncher.launch("application/json")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Upload JSON")
                }
                
                Button(
                    onClick = {
                        exportJsonLauncher.launch("schema.json")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Download JSON")
                }
            }

            // File Actions SQL
            Text("SQL File Operations", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        importSqlLauncher.launch("*/*")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Upload SQL")
                }
                
                Button(
                    onClick = {
                        exportSqlLauncher.launch("schema.sql")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Download SQL")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = textOutput,
                onValueChange = {},
                readOnly = true,
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
