package ru.prmo.exception

import org.springframework.http.HttpStatus

class UserAlreadyExistsException : BaseException(
    httpStatus = HttpStatus.CONFLICT,
    apiError = ApiError(
        errorCode = "user.already.exists",
        description = "ѕользователь с таким именем уже существует"
    )
) {
}