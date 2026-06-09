package com.example.data

object SchemaGenerator {

    fun generateBetterSqlite3Script(tables: List<TableWithColumns>): String {
        return buildString {
            appendLine("const Database = require('better-sqlite3');")
            appendLine("const db = new Database('database.db', { verbose: console.log });")
            appendLine()
            appendLine("db.exec(`")
            append(generateRawSql(tables))
            appendLine("`);")
            appendLine()
            appendLine("// --- CRUD GENERATED CODE ---")
            for (tableWithCol in tables) {
                val tableName = tableWithCol.table.name
                val columns = tableWithCol.columns
                val firstPk = columns.firstOrNull { it.isPrimaryKey }?.name ?: "id"
                
                val colsNoAutoInc = columns.filter { !(it.isPrimaryKey && it.isAutoIncrement) }
                val colNamesParams = colsNoAutoInc.joinToString(", ") { "@${it.name}" }
                val colNamesArgs = colsNoAutoInc.joinToString(", ") { it.name }
                
                val capitalizedName = tableName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                
                appendLine("const insert$capitalizedName = db.prepare('INSERT INTO $tableName ($colNamesArgs) VALUES ($colNamesParams)');")
                appendLine("const getAll$capitalizedName = db.prepare('SELECT * FROM $tableName');")
                appendLine("const get${capitalizedName}ById = db.prepare('SELECT * FROM $tableName WHERE $firstPk = ?');")
                
                val updateSets = colsNoAutoInc.joinToString(", ") { "${it.name} = @${it.name}" }
                appendLine("const update$capitalizedName = db.prepare('UPDATE $tableName SET $updateSets WHERE $firstPk = @$firstPk');")
                appendLine("const delete$capitalizedName = db.prepare('DELETE FROM $tableName WHERE $firstPk = ?');")
                appendLine()
            }
            appendLine("module.exports = {")
            appendLine("  db,")
            for (tableWithCol in tables) {
                val capitalizedName = tableWithCol.table.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                appendLine("  insert$capitalizedName,")
                appendLine("  getAll$capitalizedName,")
                appendLine("  get${capitalizedName}ById,")
                appendLine("  update$capitalizedName,")
                appendLine("  delete$capitalizedName,")
            }
            appendLine("};")
        }
    }

    fun generateNodeSqliteScript(tables: List<TableWithColumns>): String {
        return buildString {
            appendLine("import { DatabaseSync } from 'node:sqlite';")
            appendLine("const db = new DatabaseSync('database.db');")
            appendLine()
            appendLine("db.exec(`")
            append(generateRawSql(tables))
            appendLine("`);")
            appendLine()
            appendLine("// --- CRUD GENERATED CODE ---")
            for (tableWithCol in tables) {
                val tableName = tableWithCol.table.name
                val columns = tableWithCol.columns
                val firstPk = columns.firstOrNull { it.isPrimaryKey }?.name ?: "id"
                
                val colsNoAutoInc = columns.filter { !(it.isPrimaryKey && it.isAutoIncrement) }
                val questionMarks = colsNoAutoInc.joinToString(", ") { "?" }
                val colNamesArgs = colsNoAutoInc.joinToString(", ") { it.name }
                
                val capitalizedName = tableName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                
                appendLine("const insert$capitalizedName = db.prepare('INSERT INTO $tableName ($colNamesArgs) VALUES ($questionMarks)');")
                appendLine("const getAll$capitalizedName = db.prepare('SELECT * FROM $tableName');")
                appendLine("const get${capitalizedName}ById = db.prepare('SELECT * FROM $tableName WHERE $firstPk = ?');")
                
                val updateSets = colsNoAutoInc.joinToString(", ") { "${it.name} = ?" }
                appendLine("const update$capitalizedName = db.prepare('UPDATE $tableName SET $updateSets WHERE $firstPk = ?');")
                appendLine("const delete$capitalizedName = db.prepare('DELETE FROM $tableName WHERE $firstPk = ?');")
                appendLine()
            }
            appendLine("export {")
            appendLine("  db,")
            for (tableWithCol in tables) {
                val capitalizedName = tableWithCol.table.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                appendLine("  insert$capitalizedName,")
                appendLine("  getAll$capitalizedName,")
                appendLine("  get${capitalizedName}ById,")
                appendLine("  update$capitalizedName,")
                appendLine("  delete$capitalizedName,")
            }
            appendLine("};")
        }
    }

    fun generateSqlite3Script(tables: List<TableWithColumns>): String {
        return buildString {
            appendLine("const sqlite3 = require('sqlite3').verbose();")
            appendLine("const db = new sqlite3.Database('database.db');")
            appendLine()
            appendLine("db.serialize(() => {")
            appendLine("  db.exec(`")
            append(generateRawSql(tables))
            appendLine("  `);")
            appendLine("});")
            appendLine()
            appendLine("// --- CRUD GENERATED CODE ---")
            for (tableWithCol in tables) {
                val tableName = tableWithCol.table.name
                val columns = tableWithCol.columns
                val firstPk = columns.firstOrNull { it.isPrimaryKey }?.name ?: "id"
                
                val colsNoAutoInc = columns.filter { !(it.isPrimaryKey && it.isAutoIncrement) }
                val colNamesParams = colsNoAutoInc.joinToString(", ") { "?" }
                val colNamesArgs = colsNoAutoInc.joinToString(", ") { it.name }
                
                val capitalizedName = tableName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                
                appendLine("function insert$capitalizedName(data, callback) {")
                appendLine("  const stmt = db.prepare('INSERT INTO $tableName ($colNamesArgs) VALUES ($colNamesParams)');")
                appendLine("  stmt.run([${colsNoAutoInc.joinToString(", ") { "data.${it.name}" }}], function(err) {")
                appendLine("    callback(err, this.lastID);")
                appendLine("  });")
                appendLine("  stmt.finalize();")
                appendLine("}")
                appendLine()
            }
            appendLine("module.exports = {")
            appendLine("  db,")
            for (tableWithCol in tables) {
                val capitalizedName = tableWithCol.table.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                appendLine("  insert$capitalizedName,")
            }
            appendLine("};")
        }
    }

    fun generateRawSql(tables: List<TableWithColumns>): String {
        return buildString {
            appendLine("  -- Database Schema")
            for (tableWithCol in tables) {
                appendLine("  CREATE TABLE IF NOT EXISTS ${tableWithCol.table.name} (")
                
                val columns = tableWithCol.columns
                for ((index, col) in columns.withIndex()) {
                    append("    ${col.name} ${col.type}")
                    
                    if (col.isPrimaryKey) {
                        append(" PRIMARY KEY")
                        if (col.isAutoIncrement) {
                            append(" AUTOINCREMENT")
                        }
                    }
                    
                    if (col.isNotNull) {
                        append(" NOT NULL")
                    }
                    if (col.isUnique) {
                        append(" UNIQUE")
                    }
                    if (col.defaultValue.isNotBlank()) {
                        append(" DEFAULT ${col.defaultValue}")
                    }
                    if (col.foreignKeyReferences.isNotBlank()) {
                        append(" REFERENCES ${col.foreignKeyReferences}")
                    }
                    
                    if (index < columns.size - 1) {
                        append(",")
                    }
                    appendLine()
                }
                
                appendLine("  );")
                appendLine()
            }
        }
    }
}
