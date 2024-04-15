package ru.prmo.dto

import java.time.LocalDate

data class AdminDailyTotalDto(
    var date: LocalDate,
    var departmentName: String,
    var total: Int,
    var operationRecords: List<OperationRecordDto>,
    var stringOperationRecords: List<StringOperationRecordDto>
)
