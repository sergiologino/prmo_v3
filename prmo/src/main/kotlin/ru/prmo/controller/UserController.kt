package ru.prmo.controller

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import ru.prmo.dto.DailyTotalDto
import ru.prmo.dto.OperationRecordDto
import ru.prmo.dto.StringOperationRecordDto
import ru.prmo.dto.UserDataDto
import ru.prmo.exception.BadDayToSendData
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
            defaultValue = "#{T(java.time.LocalDate).now().minusDays(1)}"
        ) @DateTimeFormat(pattern = "yyyy-MM-dd") date: LocalDate, model: Model, principal: Principal
    ): String {
        val currentUser = userService.findByUsername(principal.name)

        val dailyTotal = dailyTotalService.getDailyTotalByDateAndDepartment(date, currentUser!!.department!!)!!
        if (dailyTotal.operationRecords.isEmpty()) {
            val operations = departmentService.getDepartmentByUser(principal).operations.map { it.operationName }
            for (operation in operations) {
                if (operation.contains("(да/нет)", ignoreCase = true)) {
                    dailyTotal.addStringRecord(StringOperationRecordDto(operationName = operation))
                } else {
                    dailyTotal.addRecord(OperationRecordDto(operationName = operation))
                }
            }
        }

//        println(date)
//        model["currentDate"] = dailyTotal.date
        model["form"] = dailyTotal
        model["userData"] = UserDataDto(
            username = currentUser.username,
            departmentName = currentUser.department!!.departmentName
        )
        return "user-workspace"
    }

    @PostMapping("workspace")
    fun createNewDailyTotal(
        @Valid @ModelAttribute("form") dailyTotal: DailyTotalDto,
        errors: Errors,
        model: Model,
        principal: Principal,
        redirectAttributes: RedirectAttributes
    ): String {

        val currentDate = dailyTotal.date

        if (dailyTotal.date!!.isBefore(LocalDate.now().minusDays(1))) {
            throw BadDayToSendData()
        } else {
            dailyTotalService.createDailyTotal(dailyTotal, principal)
        }

        redirectAttributes.addFlashAttribute("success", "Данные успешно отправлены!")
        return "redirect:/user/workspace?date=$currentDate"
    }
}