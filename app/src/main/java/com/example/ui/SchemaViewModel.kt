package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AiColumnGenerator
import com.example.data.ColumnEntity
import com.example.data.SchemaDatabase
import com.example.data.SchemaGenerator
import com.example.data.SchemaRepository
import com.example.data.TableEntity
import com.example.data.TableWithColumns
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SchemaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        SchemaDatabase::class.java, "schema-db"
    ).fallbackToDestructiveMigration().build()

    private val repository = SchemaRepository(db.schemaDao())

    val allTables: StateFlow<List<TableWithColumns>> = repository.allTablesWithColumns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** True if another table already uses [name] (case-insensitive, trimmed). */
    fun isTableNameTaken(name: String, excludeTableId: Long? = null): Boolean {
        val target = name.trim().lowercase()
        if (target.isEmpty()) return false
        return allTables.value.any {
            it.table.tableId != excludeTableId && it.table.name.trim().lowercase() == target
        }
    }

    fun addTable(name: String) {
        val clean = name.trim()
        if (clean.isEmpty() || isTableNameTaken(clean)) return
        viewModelScope.launch {
            repository.addTable(clean)
        }
    }

    fun updateTable(table: TableEntity) {
        val clean = table.name.trim()
        if (clean.isEmpty() || isTableNameTaken(clean, excludeTableId = table.tableId)) return
        viewModelScope.launch {
            repository.updateTable(table.copy(name = clean))
        }
    }

    fun deleteTable(table: TableEntity) {
        viewModelScope.launch {
            repository.deleteTable(table)
        }
    }

    fun addColumn(tableId: Long) {
        val existing = allTables.value.find { it.table.tableId == tableId }
            ?.columns?.map { it.name.trim().lowercase() } ?: emptyList()
        var name = "new_column"
        var i = 1
        while (existing.contains(name.lowercase())) {
            name = "new_column_$i"
            i++
        }
        val uniqueName = name
        viewModelScope.launch {
            repository.addColumn(
                ColumnEntity(
                    tableId = tableId,
                    name = uniqueName,
                    type = "TEXT"
                )
            )
        }
    }

    fun autoGenerateColumns(tableId: Long, tableName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val existingTables = allTables.value.map { it.table.name }
                val newCols = AiColumnGenerator.generateColumns(tableName, existingTables)
                for (col in newCols) {
                    repository.addColumn(col.copy(tableId = tableId))
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateColumn(column: ColumnEntity) {
        viewModelScope.launch {
            repository.updateColumn(column)
        }
    }

    fun deleteColumn(column: ColumnEntity) {
        viewModelScope.launch {
            repository.deleteColumn(column)
        }
    }

    suspend fun getExportedJson(): String {
        return repository.exportSchemaToJson()
    }

    suspend fun importFromJson(json: String): Boolean {
        return repository.importSchemaFromJson(json)
    }

    fun getGeneratedScript(scriptType: String = "better-sqlite3"): String {
        return when (scriptType) {
            "node:sqlite" -> SchemaGenerator.generateNodeSqliteScript(allTables.value)
            "sqlite3" -> SchemaGenerator.generateSqlite3Script(allTables.value)
            "raw_sql" -> SchemaGenerator.generateRawSql(allTables.value)
            else -> SchemaGenerator.generateBetterSqlite3Script(allTables.value)
        }
    }

    suspend fun getExportedSql(): String {
        return SchemaGenerator.generateRawSql(allTables.value)
    }

    suspend fun importFromSql(sql: String): Boolean {
        return repository.importSchemaFromSql(sql)
    }
}
