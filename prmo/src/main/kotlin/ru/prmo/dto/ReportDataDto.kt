package ru.prmo.dto

import java.time.LocalDate

data class ReportDataDto(
    var fileName: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
