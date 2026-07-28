package com.ato.helpers

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun formatDate(newDate: Long): String {
    val instant = Instant.fromEpochMilliseconds(newDate)
    val timeZone = TimeZone.currentSystemDefault()
    val localDateTime = instant.toLocalDateTime(timeZone)

    val day = localDateTime.dayOfMonth
    val monthName = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val year = localDateTime.year

   return "$day $monthName $year"
}

fun formatDate(newDate: Double): String {
    val instant = Instant.fromEpochMilliseconds(newDate.toLong())
    val timeZone = TimeZone.currentSystemDefault()
    val localDateTime = instant.toLocalDateTime(timeZone)

    val day = localDateTime.dayOfMonth
    val monthName = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val year = localDateTime.year

    return "$day $monthName $year"
}