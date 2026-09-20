package com.example

import com.example.util.BracketStyle
import com.example.util.MagicTextCategory
import com.example.util.MagicTextEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBracketFormatting() {
        val curly = BracketStyle.CURLY
        val square = BracketStyle.SQUARE

        assertEquals("{battery}", curly.formatToken("battery"))
        assertEquals("[battery]", square.formatToken("battery"))

        assertEquals("{hour_24}:{minute}", curly.formatTemplate("[hour_24]:[minute]"))
        assertEquals("[hour_24]:[minute]", square.formatTemplate("{hour_24}:{minute}"))
    }

    @Test
    fun testMagicTextLibraries() {
        // Test All Magic Text reference catalog
        assertTrue(MagicTextEvaluator.allMagicTextTokens.isNotEmpty())
        assertTrue(MagicTextEvaluator.allMagicTextTokens.any { it.category == MagicTextCategory.DATE_TIME })
        assertTrue(MagicTextEvaluator.allMagicTextTokens.any { it.token == "battery" })

        // Test Combinations library
        assertTrue(MagicTextEvaluator.commonCombinations.isNotEmpty())
        assertTrue(MagicTextEvaluator.commonCombinations.any { it.name.contains("Date") })
    }

    @Test
    fun testMagicTextEvaluation() {
        val resultCurly = MagicTextEvaluator.getEvaluatedInfo(
            rawName = "[date_month_short] [date_day], [date_year]",
            tags = "date, time",
            bracketStyle = BracketStyle.CURLY
        )

        assertEquals("Date", resultCurly.label)
        assertEquals("{date_month_short} {date_day}, {date_year}", resultCurly.formattedName)
        assertEquals(3, resultCurly.subTokens.size)
        assertTrue(resultCurly.isLiveEvaluated)

        val resultSquare = MagicTextEvaluator.getEvaluatedInfo(
            rawName = "{battery}",
            tags = "device",
            bracketStyle = BracketStyle.SQUARE
        )
        assertEquals("[battery]", resultSquare.formattedName)
    }
}
