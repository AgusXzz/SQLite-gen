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

    fun addTable(name: String) {
        viewModelScope.launch {
            repository.addTable(name)
        }
    }

    fun updateTable(table: TableEntity) {
        viewModelScope.launch {
            repository.updateTable(table)
        }
    }

    fun deleteTable(table: TableEntity) {
        viewModelScope.launch {
            repository.deleteTable(table)
        }
    }

    fun addColumn(tableId: Long) {
        viewModelScope.launch {
            repository.addColumn(
                ColumnEntity(
                    tableId = tableId,
                    name = "new_column",
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
