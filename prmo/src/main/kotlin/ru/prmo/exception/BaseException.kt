package ru.prmo.exception

import org.springframework.http.HttpStatus
import java.lang.RuntimeException

abstract class BaseException(
    val httpStatus: HttpStatus,
    val apiError: ApiError,
): RuntimeException(apiError.description)