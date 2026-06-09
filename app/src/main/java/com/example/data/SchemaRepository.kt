package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SchemaRepository(private val dao: SchemaDao) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val schemaAdapter = moshi.adapter(ExportedSchema::class.java)

    val allTablesWithColumns: Flow<List<TableWithColumns>> = dao.getAllTablesWithColumns()

    fun getTableById(tableId: Long): Flow<TableEntity?> = dao.getTableById(tableId)
    fun getColumnsForTable(tableId: Long): Flow<List<ColumnEntity>> = dao.getColumnsForTable(tableId)

    suspend fun addTable(name: String): Long {
        return dao.insertTable(TableEntity(name = name))
    }

    suspend fun updateTable(table: TableEntity) {
        dao.updateTable(table)
    }

    suspend fun deleteTable(table: TableEntity) {
        dao.deleteTable(table)
    }

    suspend fun addColumn(column: ColumnEntity) {
        dao.insertColumn(column)
    }

    suspend fun updateColumn(column: ColumnEntity) {
        dao.updateColumn(column)
    }

    suspend fun deleteColumn(column: ColumnEntity) {
        dao.deleteColumn(column)
    }

    suspend fun exportSchemaToJson(): String {
        val tablesWithCols = dao.getAllTablesWithColumns().first()
        val exportedTables = tablesWithCols.map { tableWithCols ->
            ExportedTable(
                name = tableWithCols.table.name,
                columns = tableWithCols.columns.map { c ->
                    ExportedColumn(
                        name = c.name,
                        type = c.type,
                        isPrimaryKey = c.isPrimaryKey,
                        isAutoIncrement = c.isAutoIncrement,
                        isNotNull = c.isNotNull,
                        isUnique = c.isUnique,
                        defaultValue = c.defaultValue,
                        foreignKeyReferences = c.foreignKeyReferences
                    )
                }
            )
        }
        val exportedSchema = ExportedSchema(tables = exportedTables)
        return schemaAdapter.indent("  ").toJson(exportedSchema)
    }

    suspend fun importSchemaFromJson(json: String): Boolean {
        try {
            val exportedSchema = schemaAdapter.fromJson(json) ?: return false
            // Clear existing
            dao.clearAllTables()
            
            // Insert newly imported
            for (et in exportedSchema.tables) {
                val tableId = dao.insertTable(TableEntity(name = et.name))
                for (ec in et.columns) {
                    dao.insertColumn(
                        ColumnEntity(
                            tableId = tableId,
                            name = ec.name,
                            type = ec.type,
                            isPrimaryKey = ec.isPrimaryKey,
                            isAutoIncrement = ec.isAutoIncrement,
                            isNotNull = ec.isNotNull,
                            isUnique = ec.isUnique,
                            defaultValue = ec.defaultValue,
                            foreignKeyReferences = ec.foreignKeyReferences
                        )
                    )
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun importSchemaFromSql(sql: String): Boolean {
        try {
            dao.clearAllTables()
            val createTableRegex = Regex("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([\\w]+)\\s*\\(([^;]+)\\);", RegexOption.IGNORE_CASE)
            val matches = createTableRegex.findAll(sql)
            for (match in matches) {
                val tableName = match.groupValues[1]
                val columnsStr = match.groupValues[2]
                
                val tableId = dao.insertTable(TableEntity(name = tableName))
                
                val cols = columnsStr.split(Regex(",(?![^()]*\\))"))
                for (colStr in cols) {
                    val trimmed = colStr.trim()
                    if (trimmed.isEmpty() || trimmed.uppercase().startsWith("PRIMARY") || trimmed.uppercase().startsWith("FOREIGN") || trimmed.uppercase().startsWith("UNIQUE")) continue
                    
                    val parts = trimmed.split(Regex("\\s+"))
                    if (parts.size >= 2) {
                        val name = parts[0]
                        val type = parts[1].uppercase()
                        val isPk = trimmed.uppercase().contains("PRIMARY KEY")
                        val isAuto = trimmed.uppercase().contains("AUTOINCREMENT")
                        val isNotN = trimmed.uppercase().contains("NOT NULL")
                        val isUniq = trimmed.uppercase().contains("UNIQUE")
                        
                        val defValMatch = Regex("DEFAULT\\s+([\\w'\"\\.]+)").find(trimmed.uppercase())
                        val defVal = defValMatch?.groupValues?.get(1) ?: ""
                        
                        val refMatch = Regex("REFERENCES\\s+([\\w\\(\\)]+)").find(trimmed.uppercase())
                        val ref = refMatch?.groupValues?.get(1) ?: ""
                        
                        dao.insertColumn(ColumnEntity(
                            tableId = tableId,
                            name = name,
                            type = type,
                            isPrimaryKey = isPk,
                            isAutoIncrement = isAuto,
                            isNotNull = isNotN,
                            isUnique = isUniq,
                            defaultValue = defVal,
                            foreignKeyReferences = ref
                        ))
                    }
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
