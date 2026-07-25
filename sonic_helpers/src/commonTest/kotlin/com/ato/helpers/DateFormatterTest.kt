package com.ato.helpers

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateFormatterTest {

    private val sampleEpochMillis = 1_700_000_000_000L

    @Test
    fun formatDate_long_matchesDayMonthYearShape() {
        assertEquals(expectedFormat(sampleEpochMillis), formatDate(sampleEpochMillis))
    }

    @Test
    fun formatDate_doubleOverload_matchesLongOverload() {
        assertEquals(formatDate(sampleEpochMillis), formatDate(sampleEpochMillis.toDouble()))
    }

    @Test
    fun formatDate_capitalizesOnlyFirstLetterOfMonth() {
        val monthName = formatDate(sampleEpochMillis).split(" ")[1]

        assertTrue(monthName.first().isUpperCase())
        assertEquals(monthName.drop(1), monthName.drop(1).lowercase())
    }

    private fun expectedFormat(epochMillis: Long): String {
        val localDateTime = Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())
        val monthName = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
        return "${localDateTime.dayOfMonth} $monthName ${localDateTime.year}"
    }
}
