package com.example.data

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AiColumnGeneratorTest {
    @Test
    fun testGenerate() = runBlocking {
        try {
            val result = AiColumnGenerator.generateColumns("users", listOf())
            println("Result: $result")
            assertTrue(result.isNotEmpty())
        } catch (e: Exception) {
            e.printStackTrace()
            fail(e.message)
        }
    }
}
