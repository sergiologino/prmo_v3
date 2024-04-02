package ru.prmo.exception

import org.springframework.http.HttpStatus

class OneDatasetPerDateException: BaseException(
    httpStatus = HttpStatus.BAD_REQUEST,
    apiError = ApiError(
        errorCode = "one.dataset.per.date",
        description = "Данные на текущую дату уже были отправлены. За более подробной информацией обратитесь к администратору..."
    )
) {
}