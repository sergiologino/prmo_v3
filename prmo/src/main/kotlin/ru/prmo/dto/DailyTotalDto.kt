package ru.prmo.dto

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DailyTotalDto(
    var operationRecords: MutableList<OperationRecordDto> = mutableListOf(),
    var date: LocalDate?     // format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
) {
    fun addRecord(record: OperationRecordDto) {
        this.operationRecords.add(record)
    }
}
