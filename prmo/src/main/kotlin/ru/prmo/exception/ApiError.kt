package ru.prmo.exception

data class ApiError(
    val errorCode: String,
    val description: String
)
