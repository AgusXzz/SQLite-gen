package com.example.data

import androidx.room.*
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey(autoGenerate = true) val tableId: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "columns",
    foreignKeys = [
        ForeignKey(
            entity = TableEntity::class,
            parentColumns = ["tableId"],
            childColumns = ["tableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tableId")]
)
data class ColumnEntity(
    @PrimaryKey(autoGenerate = true) val columnId: Long = 0,
    val tableId: Long,
    val name: String,
    val type: String, // TEXT, INTEGER, REAL, BLOB, NUMERIC
    val isPrimaryKey: Boolean = false,
    val isAutoIncrement: Boolean = false,
    val isNotNull: Boolean = false,
    val isUnique: Boolean = false,
    val defaultValue: String = "",
    val foreignKeyReferences: String = ""
)

data class TableWithColumns(
    @Embedded val table: TableEntity,
    @Relation(
        parentColumn = "tableId",
        entityColumn = "tableId"
    )
    val columns: List<ColumnEntity>
)

@Dao
interface SchemaDao {
    @Transaction
    @Query("SELECT * FROM tables ORDER BY createdAt ASC")
    fun getAllTablesWithColumns(): Flow<List<TableWithColumns>>

    @Query("SELECT * FROM tables WHERE tableId = :tableId")
    fun getTableById(tableId: Long): Flow<TableEntity?>

    @Query("SELECT * FROM columns WHERE tableId = :tableId ORDER BY columnId ASC")
    fun getColumnsForTable(tableId: Long): Flow<List<ColumnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity): Long

    @Update
    suspend fun updateTable(table: TableEntity)

    @Delete
    suspend fun deleteTable(table: TableEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumn(column: ColumnEntity): Long

    @Update
    suspend fun updateColumn(column: ColumnEntity)

    @Delete
    suspend fun deleteColumn(column: ColumnEntity)

    @Query("DELETE FROM tables")
    suspend fun clearAllTables()
}

@Database(entities = [TableEntity::class, ColumnEntity::class], version = 1, exportSchema = false)
abstract class SchemaDatabase : RoomDatabase() {
    abstract fun schemaDao(): SchemaDao
}

// Intermediary models for Moshi JSON Export/Import
@JsonClass(generateAdapter = true)
data class ExportedSchema(
    val tables: List<ExportedTable>
)

@JsonClass(generateAdapter = true)
data class ExportedTable(
    val name: String,
    val columns: List<ExportedColumn>
)

@JsonClass(generateAdapter = true)
data class ExportedColumn(
    val name: String,
    val type: String,
    val isPrimaryKey: Boolean,
    val isAutoIncrement: Boolean,
    val isNotNull: Boolean,
    val isUnique: Boolean,
    val defaultValue: String,
    val foreignKeyReferences: String = ""
)
