package ru.prmo.dto

data class UserRegistrationDto(
    var username: String = "",
    var password: String? = null,
    var department: String? = null
)
