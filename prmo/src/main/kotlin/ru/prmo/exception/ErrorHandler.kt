package ru.prmo.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ErrorHandler: ResponseEntityExceptionHandler() {
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException): ResponseEntity<ApiError> {
        return ResponseEntity(ex.apiError, ex.httpStatus)
    }
}