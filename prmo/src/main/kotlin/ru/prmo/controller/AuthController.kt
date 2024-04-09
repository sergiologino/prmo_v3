package ru.prmo.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.prmo.service.DailyTotalService
import ru.prmo.service.DepartmentService
import ru.prmo.service.UserService
import java.security.Principal

@Controller
@RequestMapping("/")
class AuthController(
    private val userService: UserService,
    private val departmentService: DepartmentService,
    private val dailyTotalService: DailyTotalService
) {

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @GetMapping("/")
    fun redirect(principal: Principal): String {
        val currentUser = userService.findByUsername(principal.name)
        val roles = currentUser.roles.map { it.roleName }
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin/panel"
        }
        return "redirect:/user/workspace"
    }


//
//    @GetMapping("/user/{date}")
//    fun testDate(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate, model: Model): String {
//        val form = dailyTotalService.getDailyTotalByDate(date)
//        model["form"] = form
//        println(date)
//        model["date"] = date
//        return "user-workspace"
////        return "test-date"
//    }
//
//    @GetMapping("user/test")
//    fun test(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate, model: Model): String {
//        println(date)
//        return "user-workspace"
////        return "test-date"
//    }

}