package ru.prmo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class MainController {

    @GetMapping("/unsecured")
    fun unsecured(): String {
        return "unsecured page"
    }

    @GetMapping("/secured")
    fun secured(): String {
        return "secured page"
    }
}