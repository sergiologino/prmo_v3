package ru.prmo.dto

import jakarta.validation.constraints.Digits

data class OperationRecordDto(
    var operationName: String? = null,
    @field:Digits(message = "Похоже, вы ввели вместо числа символ или отрицательное число", integer = 100, fraction = 0)
    var count: Int? = null
)
