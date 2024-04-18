package ru.prmo.controller


import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import ru.prmo.dto.*
import ru.prmo.exception.UserAlreadyExistsException
import ru.prmo.service.DailyTotalService
import ru.prmo.service.DepartmentService
import ru.prmo.service.OperationService
import ru.prmo.service.UserService
import ru.prmo.utils.ExcelWriter
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.Principal
import java.time.LocalDate


@Controller
@RequestMapping("admin")
class AdminController(
    private val departmentService: DepartmentService,
    private val userService: UserService,
    private val dailyTotalService: DailyTotalService,
    private val operationService: OperationService
) {

    @GetMapping("registration")
    fun registration(model: Model): String {
        model["user"] = UserRegistrationDto()
        model["departments"] = departmentService.getAllDepartments().map { it.departmentName }
        return "registration"
    }

    @PostMapping("registration")
    fun createNewUser(@ModelAttribute("user") userRegistrationDto: UserRegistrationDto, model: Model): String {
        val existingUser = userService.findByUsername(userRegistrationDto.username)
        if (existingUser != null) {
            throw UserAlreadyExistsException()
        }
        userService.createNewUser(userRegistrationDto)

        return "redirect:/admin/users"
    }

    @GetMapping("panel")
    fun getAdminPanel(model: Model): String {


        model["report_data"] = ReportDataDto()
//        model["dts_form"] = dts
        return "admin-panel"
    }

    @PostMapping("panel")
    fun createReport(@ModelAttribute("report_data") reportDataDto: ReportDataDto, model: Model): String {


        val dts = dailyTotalService.getAllDailyTotals()
        val departments = departmentService.getAllDepartments().toList()

        val excelWriter = ExcelWriter()
        val filePath = excelWriter.write(departments, dts, reportDataDto)

        val file = File(filePath)
        reportDataDto.fileName = file.name
        println(file.absolutePath)
        model["report_data"] = reportDataDto
        return "admin-panel"
    }

    @Throws(IOException::class)
    @GetMapping("download")
    fun downloadReport(
        response: HttpServletResponse,
        @RequestParam(value = "fileName") fileName: String
    ) {

        val file = File(fileName)
//        val file = File("reports\\$fileName")
        response.contentType = "application/octet-stream"
        val headerKey = "Content-Disposition"
        val headerValue: String = "attachment; filename=" + file.name
        response.setHeader(headerKey, headerValue)
        val outputStream: ServletOutputStream = response.outputStream
        val inputStream = BufferedInputStream(FileInputStream(file))
        val buffer = ByteArray(1024)
        var bytesRead = 0
        while (bytesRead != -1) {
            bytesRead = inputStream.read(buffer)
            outputStream.write(buffer, 0, bytesRead)
        }
        inputStream.close()
        outputStream.close()
    }

    @GetMapping("users")
    fun getUsers(model: Model): String {
        val allUsers = userService.findAll().map {
            UserDataDto(
                username = it.username,
                departmentName = it.department?.departmentName ?: ""
            )
        }
        model["usersForm"] = allUsers
        return "users"
    }

    @GetMapping("departments")
    fun getDepartments(model: Model): String {
        model["departments"] = departmentService.getAllDepartments().map {
            DepartmentDto(
                departmentName = it.departmentName
            )
        }
        return "departments"
    }

    @GetMapping("operations")
    fun getOperations(model: Model): String {
        model["operations"] = operationService.getAllOperations().map {
            OperationDto(
                operationName = it.operationName
            )
        }
        return "operations"
    }

    @GetMapping("edit")
    fun searchDailyTotalForEdit(
        @RequestParam(
            value = "date",
            name = "date",
            required = false,
            defaultValue = "#{T(java.time.LocalDate).now().minusDays(1)}"
        ) date: LocalDate,
        @RequestParam(
            value = "departmentName",
            name = "departmentName",
            required = false,
            defaultValue = "start"
        ) departmentName: String,
        model: Model
    ): String {
        var dailyTotal = DailyTotalDto(date = date)

        if (departmentName != "start") {
            val depEntity = departmentService.getDepartmentByName(departmentName)
            dailyTotal = dailyTotalService.getDailyTotalByDateAndDepartment(
                date = date,
                departmentEntity = depEntity
            )!!
            if (dailyTotal.operationRecords.isEmpty()) {
                val operations = depEntity.operations.map { it.operationName }
                for (operation in operations) {
                    if (operation.contains("да/нет")) {
                        dailyTotal.addStringRecord(StringOperationRecordDto(operationName = operation))
                    } else {
                        dailyTotal.addRecord(OperationRecordDto(operationName = operation))
                    }
                }
            }
        }
        model["form"] = dailyTotal
        model["departments"] = departmentService.getAllDepartments().map { it.departmentName }

        return "edit-dailytotals"
    }

    @PostMapping("edit")
    fun editDailyTotal(
        @ModelAttribute("form") dailyTotal: DailyTotalDto,
        model: Model,
        principal: Principal
    ): String {
        val department = departmentService.getDepartmentByName(dailyTotal.departmentName!!)
        dailyTotalService.editDailyTotalByAdmin(dailyTotal, principal, department)
        return "redirect:/admin/panel"
    }

}