package ru.prmo.dto

import java.time.LocalDate

data class DailyTotalDto(
    var operationRecords: MutableList<OperationRecordDto> = mutableListOf(),
//    var date: LocalDate = LocalDate.now()
) {
    fun addRecord(record: OperationRecordDto) {
        this.operationRecords.add(record)
    }
}
