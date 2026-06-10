package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object AiColumnGenerator {
    private val client = OkHttpClient()

    suspend fun generateColumns(tableName: String, existingTables: List<String>): List<ColumnEntity> = withContext(Dispatchers.IO) {
        val existingStr = if (existingTables.isNotEmpty()) "Tabel lain yang sudah ada: ${existingTables.joinToString(", ")}." else ""
        val prompt = "Buatkan schema kolom secara otomatis untuk tabel bernama '$tableName'. " +
                "$existingStr " +
                "Kembalikan hanya JSON sesuai schema. Tentukan primary key, berikan id atau uuid yang relevan. Jika ada relasi dengan tabel lain, set foreignKeyReferences (contoh 'users(id)'). Gunakan data type SQLite seperti TEXT: Used if the type name contains strings like CHAR, CLOB, or VARCHAR.NUMERIC: Used if the type name contains NUMERIC, DECIMAL, BOOLEAN, DATE, or DATETIME.INTEGER: Used if the type name contains INT (e.g., INT, BIGINT, TINYINT).REAL: Used if the type name contains REAL, DOUBLE, or FLOAT.NONE (or BLOB): Used if no type is declared or if the type name contains BLOB"
        
        val jsonObj = JSONObject().apply {
            put("gameId", "6118a90e-082c-4130-803b-7bae405f5145")
            put("versionId", "5ae1e0f4-67ec-4b46-a0ff-227156472d0e")
            
            val messages = org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are an expert helper in the field of SQL, you can create the code sqlite/sqlite3/node:sqlite3/better-sqlite3, and you are very clever in processing the code so as not to create bugs.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }
            put("messages", messages)
            
            val schema = JSONObject("""
            {
                "type": "object",
                "properties": {
                    "tableDescription": {
                        "type": "string",
                        "description": "A short summary explaining the purpose of this table and its role in the database."
                    },
                    "tips": {
                        "type": "string",
                        "description": "Additional SQLite best practices or indexing tips relevant to this schema."
                    },
                    "code": {
                        "type": "string",
                        "description": "JSON array of column objects. E.g. [{\"name\": \"id\", \"type\": \"INTEGER\", \"isPrimaryKey\": true, \"isAutoIncrement\": true, \"isNotNull\": true, \"isUnique\": true, \"defaultValue\": \"\", \"foreignKeyReferences\": \"\"}]. Only valid JSON array."
                    }
                },
                "required": ["tableDescription", "tips", "code"],
                "additionalProperties": false
            }
            """.trimIndent())
            put("responseSchema", schema)
        }
        
        val reqBody = jsonObj.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val request = Request.Builder()
            .url("https://d1a7k3p.sekai.chat/game/gen-text")
            .post(reqBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoidGVtcG9yYXJ5X2dhbWUiLCJhcHBsaWNhbnRfaWQiOjEyNDIyNjIzLCJleHAiOjE3ODM0MzI1Mzl9.PU_p2Ut1leJ5DS27oTb15pu01LBFLzC0JmKwJLSCisI")
            .build()
            
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val err = response.body?.string() ?: ""
            if (response.code == 429) {
                throw Exception("Rate Limit Reached: Limit pemanggilan AI sudah habis. Silakan coba lagi nanti.")
            }
            throw Exception("AI API call failed: ${response.code} $err")
        }
        
        val bodyStr = response.body?.string() ?: "{}"
        
        var colsArray: org.json.JSONArray? = null
        try {
            val resJson = JSONObject(bodyStr)
            var targetStr = ""

            if (resJson.has("code")) {
                targetStr = resJson.getString("code")
            } else if (resJson.has("reply")) {
                val innerReply = JSONObject(resJson.getString("reply"))
                if (innerReply.has("code")) {
                    targetStr = innerReply.getString("code")
                }
            } else {
                targetStr = bodyStr
            }
            
            val cleanStr = targetStr.replace(Regex("```json|```"), "").trim()
            colsArray = org.json.JSONArray(cleanStr)
        } catch (e: Exception) {
            // fallback
            try {
               colsArray = JSONObject(bodyStr).optJSONArray("code")
            } catch (e2: Exception) {}
        }
        
        if (colsArray == null) {
            throw Exception("Failed to parse AI response.")
        }
        
        val list = mutableListOf<ColumnEntity>()
        for (i in 0 until colsArray.length()) {
            val itm = colsArray.getJSONObject(i)
            list.add(
                 ColumnEntity(
                     tableId = 0L,
                     name = itm.optString("name", "col_$i"),
                     type = itm.optString("type", "TEXT"),
                     isPrimaryKey = itm.optBoolean("isPrimaryKey", false),
                     isAutoIncrement = itm.optBoolean("isAutoIncrement", false),
                     isNotNull = itm.optBoolean("isNotNull", false),
                     isUnique = itm.optBoolean("isUnique", false),
                     defaultValue = itm.optString("defaultValue", ""),
                     foreignKeyReferences = itm.optString("foreignKeyReferences", "")
                 )
            )
        }
        list
    }
}
