package ru.prmo.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import ru.prmo.dto.DailyTotalDto
import ru.prmo.dto.OperationRecordDto
import ru.prmo.service.DailyTotalService
import ru.prmo.service.DepartmentService
import ru.prmo.service.UserService
import java.security.Principal
import java.time.LocalDate

@Controller
@RequestMapping("user")
class UserController(
    private val userService: UserService,
    private val departmentService: DepartmentService,
    private val dailyTotalService: DailyTotalService
) {

    @GetMapping("workspace")
    fun getUserWorkspace(
        @RequestParam(
            value = "date",
            name = "date",
            defaultValue = "#{T(java.time.LocalDateTime).now()}"
        ) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate, model: Model, principal: Principal
    ): String {
        val currentUser = userService.findByUsername(principal.name)
//        val department = departmentService.getDepartmentById(currentUser.department!!.departmentId)
//        lateinit var dailyTotal: DailyTotalDto
        val dailyTotal = dailyTotalService.getDailyTotalByDateAndDepartment(date, currentUser.department!!)!!
        if (date == LocalDate.now() && dailyTotal.operationRecords.isEmpty()) {
            val operations = departmentService.getDepartmentByUser(principal).operations.map { it.operationName }
//            dailyTotal = DailyTotalDto(
//                date = date.toString()
//            )
            for (operation in operations) {
                dailyTotal.addRecord(OperationRecordDto(operationName = operation))

            }
        }

//        println(date)
//        model["currentDate"] = dailyTotal.date
        model["form"] = dailyTotal
        return "user-workspace"
    }

    @PostMapping("workspace")
    fun createNewDailyTotal(
        @ModelAttribute("form") dailyTotal: DailyTotalDto,
        model: Model,
        principal: Principal
    ): String {

        dailyTotalService.createDailyTotal(dailyTotal, principal)

        return "redirect:/user/workspace"
    }
}