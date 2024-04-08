package ru.prmo.utils

import java.time.LocalDate

class DateProgression(
    override val start: LocalDate,
    override val endInclusive: LocalDate,
    private val stepDays: Long = 1
) : Iterable<LocalDate>,
    ClosedRange<LocalDate> {
    override fun iterator(): Iterator<LocalDate> {
        return LocalDateIterator(start, endInclusive, stepDays)
    }

    infix fun step(days: Long) = DateProgression(start, endInclusive, days)
}

operator fun LocalDate.rangeTo(other: LocalDate) = DateProgression(this, other)