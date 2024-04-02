package ru.prmo.dto

data class UserRegistrationDto(
    var username: String? = null,
    var password: String? = null,
    var department: String? = null
)
