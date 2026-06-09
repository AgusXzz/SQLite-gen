package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SchemaViewModel
import com.example.data.TableWithColumns
import com.example.ui.theme.sqliteTypeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableListPane(
    viewModel: SchemaViewModel,
    selectedTableId: Long?,
    onNavigateToTable: (Long) -> Unit,
    onNavigateToCode: () -> Unit,
    onNavigateToImportExport: () -> Unit
) {
    val tables by viewModel.allTables.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var newTableName by remember { mutableStateOf("") }
    var tableToDelete by remember { mutableStateOf<TableWithColumns?>(null) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text("Schema Generator") },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToImportExport) {
                        Icon(Icons.Default.ImportExport, contentDescription = "Import/Export json")
                    }
                    FilledTonalIconButton(onClick = onNavigateToCode) {
                        Icon(Icons.Default.Code, contentDescription = "Generate Script")
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Table") }
            )
        }
    ) { paddingValues ->
        if (tables.isEmpty()) {
            EmptyTablesState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onAdd = { showAddDialog = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "stats") {
                    SchemaStatsHeader(
                        tableCount = tables.size,
                        columnCount = tables.sumOf { it.columns.size },
                        modifier = Modifier.animateItem()
                    )
                }
                items(tables, key = { it.table.tableId }) { tableWithCols ->
                    TableItem(
                        tableWithCols = tableWithCols,
                        selected = tableWithCols.table.tableId == selectedTableId,
                        onClick = { onNavigateToTable(tableWithCols.table.tableId) },
                        onDelete = { tableToDelete = tableWithCols },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newTableName = "" },
            icon = { Icon(Icons.Outlined.TableChart, contentDescription = null) },
            title = { Text("New Table") },
            text = {
                OutlinedTextField(
                    value = newTableName,
                    onValueChange = { newTableName = it },
                    label = { Text("Table Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    enabled = newTableName.isNotBlank(),
                    onClick = {
                        viewModel.addTable(newTableName.trim())
                        showAddDialog = false
                        newTableName = ""
                    }
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newTableName = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    tableToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { tableToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete table?") },
            text = { Text("\"${target.table.name}\" and its ${target.columns.size} column(s) will be permanently removed.") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        viewModel.deleteTable(target.table)
                        tableToDelete = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { tableToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SchemaStatsHeader(tableCount: Int, columnCount: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatBlock(value = tableCount.toString(), label = if (tableCount == 1) "table" else "tables")
            Spacer(Modifier.width(28.dp))
            StatBlock(value = columnCount.toString(), label = if (columnCount == 1) "column" else "columns")
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Outlined.TableChart,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun EmptyTablesState(modifier: Modifier = Modifier, onAdd: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.TableChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("No tables yet", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Create your first table to start designing your SQLite schema.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Add Table")
        }
    }
}

@Composable
fun TableItem(
    tableWithCols: TableWithColumns,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPrimaryKey = tableWithCols.columns.any { it.isPrimaryKey }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val containerColor =
        if (selected) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 3.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.TableChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tableWithCols.table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoPill(
                        icon = { Icon(Icons.Outlined.ViewColumn, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        text = "${tableWithCols.columns.size} cols"
                    )
                    if (hasPrimaryKey) {
                        InfoPill(
                            icon = { Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp)) },
                            text = "PK"
                        )
                    }
                }
                if (tableWithCols.columns.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        tableWithCols.columns.take(12).forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(sqliteTypeColors(col.type, isDark).container)
                            )
                        }
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Table", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun InfoPill(icon: @Composable () -> Unit, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}
