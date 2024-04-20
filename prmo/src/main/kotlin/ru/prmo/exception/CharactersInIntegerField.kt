package ru.prmo.exception

import org.springframework.http.HttpStatus

class CharactersInIntegerField : BaseException(
    httpStatus = HttpStatus.BAD_REQUEST,
    apiError = ApiError(
        errorCode = "characters.in.integer.field",
        description = "Похоже вы ввели символы в поле, которое принимает только числовые значения. Внимательно перепроверьте показания и попробуйте еще раз..."
    )
) {
}