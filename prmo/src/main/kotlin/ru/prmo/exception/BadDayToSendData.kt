package ru.prmo.exception

import org.springframework.http.HttpStatus

class BadDayToSendData : BaseException(
    httpStatus = HttpStatus.BAD_REQUEST,
    apiError = ApiError(
        errorCode = "bad.day.to.send.data",
        description = "Данные могут быть отправлены только за текущую или вчерашнюю даты. За более подробной информацией обратитесь к администратору..."
    )
) {
}