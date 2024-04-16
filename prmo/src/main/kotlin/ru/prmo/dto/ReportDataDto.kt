package ru.prmo.dto

import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class ReportDataDto(
    var fileName: String? = null,
    val startDate: String = YearMonth.now().atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    val endDate: String = YearMonth.now().atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
)
