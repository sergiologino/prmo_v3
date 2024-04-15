package ru.prmo.utils

import java.time.LocalDate

class LocalDateIterator(
    startDate: LocalDate,
    private val endDateInclusive: LocalDate,
    private val stepDays: Long
) : Iterator<LocalDate> {

    private var currentDate = startDate
    override fun hasNext(): Boolean =
        currentDate <= endDateInclusive

    override fun next(): LocalDate {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}