package ru.prmo.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import ru.prmo.dto.DailyTotalDto
import ru.prmo.dto.OperationRecordDto
import ru.prmo.dto.UserRegistrationDto
import ru.prmo.service.DailyTotalService
import ru.prmo.service.DepartmentService
import ru.prmo.service.UserService
import java.security.Principal
import java.time.LocalDate

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

    @GetMapping("/admin/registration")
    fun registration(model: Model): String {
        model["user"] = UserRegistrationDto()
        model["departments"] = departmentService.getAllDepartments().map { it.departmentName }
        return "registration"
    }

    @PostMapping("/admin/registration")
    fun createNewUser(@ModelAttribute("user") userRegistrationDto: UserRegistrationDto, model: Model): String {
        userService.createNewUser(userRegistrationDto)
        return "redirect:/test/unsecured"
    }

    @GetMapping("/admin/panel")
    fun getAdminPanel(): String {
        return "admin-panel"
    }

    @GetMapping("/user/workspace")
    fun getUserWorkspace(
        @RequestParam(
            value = "date",
            name = "date",
            defaultValue = "#{T(java.time.LocalDateTime).now()}"
        ) date: LocalDate, model: Model, principal: Principal
    ): String {
//        val currentUser = userService.findByUsername(principal.name)
//        val department = departmentService.getDepartmentById(currentUser.department!!.departmentId)
//        lateinit var dailyTotal: DailyTotalDto
        var dailyTotal = dailyTotalService.getDailyTotalByDate(date)!!
        if (date == LocalDate.now() && dailyTotal.operationRecords.isEmpty()) {
            val operations = departmentService.getDepartmentByUser(principal).operations.map { it.operationName }
            dailyTotal = DailyTotalDto()
            for (operation in operations) {
                dailyTotal.addRecord(OperationRecordDto(operationName = operation))
            }
        }

//        println(date)
        model["currentDate"] = date
        model["form"] = dailyTotal
        return "user-workspace"
    }

    @PostMapping("/user/workspace")
    fun createNewDailyTotal(
        @ModelAttribute("form") dailyTotal: DailyTotalDto,
        model: Model,
        principal: Principal
    ): String {
        dailyTotalService.createDailyTotal(dailyTotal, principal)

        return "user-workspace"
    }

    @GetMapping("/user/{date}")
    fun testDate(@PathVariable("date") @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate, model: Model): String {
        val form = dailyTotalService.getDailyTotalByDate(date)
        model["form"] = form
        println(date)
        model["date"] = date
        return "user-workspace"
//        return "test-date"
    }

    @GetMapping("user/test")
    fun test(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate, model: Model): String {
        println(date)
        return "user-workspace"
//        return "test-date"
    }

}