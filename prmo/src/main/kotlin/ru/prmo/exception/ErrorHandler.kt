package ru.prmo.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException, model: Model, request: HttpServletRequest): ModelAndView {
        val referer = request.getHeader("referer")
        val mav = ModelAndView()
        mav.addObject("httpStatus", ex.httpStatus)
        mav.addObject("apiError", ex.apiError)
        mav.addObject("referer", referer)
        mav.viewName = "error"
        return mav
    }
}