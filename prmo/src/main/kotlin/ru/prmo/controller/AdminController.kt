package ru.prmo.controller


import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import ru.prmo.dto.ReportDataDto
import ru.prmo.dto.UserRegistrationDto
import ru.prmo.service.DailyTotalService
import ru.prmo.service.DepartmentService
import ru.prmo.service.UserService
import ru.prmo.utils.findDateColumn
import ru.prmo.utils.findDepartmentOperationRow
import ru.prmo.utils.findDepartmentRow
import java.awt.Color
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import kotlin.jvm.Throws


@Controller
@RequestMapping("admin")
class AdminController(
    private val departmentService: DepartmentService,
    private val userService: UserService,
    private val dailyTotalService: DailyTotalService
) {

    @GetMapping("registration")
    fun registration(model: Model): String {
        model["user"] = UserRegistrationDto()
        model["departments"] = departmentService.getAllDepartments().map { it.departmentName }
        return "registration"
    }

    @PostMapping("registration")
    fun createNewUser(@ModelAttribute("user") userRegistrationDto: UserRegistrationDto, model: Model): String {
        userService.createNewUser(userRegistrationDto)
        return "redirect:/test/unsecured"
    }

    @GetMapping("panel")
    fun getAdminPanel(model: Model): String {



        model["report_data"] = ReportDataDto()
//        model["dts_form"] = dts
        return "admin-panel"
    }

    @PostMapping("panel")
    fun createReport(@ModelAttribute("report_data") reportDataDto: ReportDataDto, model: Model): String {

//        println(reportDataDto.pathToFile + " " + reportDataDto.startDate + " " + reportDataDto.endDate)
        val dts = dailyTotalService.getAllDailyTotals()
        val departments = departmentService.getAllDepartments().toList()


        val workbook = XSSFWorkbook()
        val sheet: XSSFSheet = workbook.createSheet("ПРМО - Отчет")


        val headerStyle = workbook.createCellStyle()
        val font = workbook.createFont()
        font.fontName = "Arial"
        font.fontHeightInPoints = 14.toShort()
        font.bold = true
        headerStyle.setFont(font)
        val colorMap = workbook.stylesSource.indexedColors
        val grey = XSSFColor(Color(255, 17, 192), colorMap)
        headerStyle.setFillForegroundColor(grey)

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Процедура/Дата")


//         Диапазон дат
//        val startDate = LocalDate.of(2024, 4, 1)
//        val endDate = LocalDate.of(2024, 4, 8)
        val startDate = reportDataDto.startDate!!
        val endDate = reportDataDto.endDate!!
        var column = 1
        var date = startDate
        while (date.isBefore(endDate.plusDays(1))) {
            headerRow.createCell(column).setCellValue(date.toString())
            sheet.autoSizeColumn(column)
            column++
            date = date.plusDays(1)
        }



        var row = 1
        for (dep in departments) {
            sheet.createRow(row).createCell(0).setCellValue(dep.departmentName)
            for (i in 1..dep.operations.size) {
                sheet.createRow(row + i).createCell(0).setCellValue(dep.operations[i-1].operationName)
            }
            sheet.createRow(row + dep.operations.size + 1).createCell(0).setCellValue("Всего")

            row += (dep.operations.size + 2)
        }

        for (dt in dts) {
            val dateColumnIndex = findDateColumn(sheet, dt.date)
            if (dateColumnIndex != -1) {
                val departmentRowIndex = findDepartmentRow(sheet, dt.departmentName)
                sheet.getRow(departmentRowIndex + dt.operationRecords.size + 1).createCell(dateColumnIndex).setCellValue(dt.total.toDouble())
                for (op in dt.operationRecords) {

                    val operationRowIndex = findDepartmentOperationRow(sheet, dt, op)
                    if (operationRowIndex != -1) {
                        sheet.getRow(operationRowIndex).createCell(dateColumnIndex)
                            .setCellValue(op.count?.toDouble() ?: 0.0)
                    }


                }
            }
        }
        val lastCellIndex = sheet.getRow(0).lastCellNum
        val lastColumnName = CellReference.convertNumToColString((lastCellIndex - 1))

        val totalCellHeader = sheet.getRow(0).createCell(lastCellIndex.toInt()).setCellValue("ИТОГО")
        for (i in 2..sheet.lastRowNum) {
            val colIndex = i + 1
            sheet.getRow(i).createCell(lastCellIndex.toInt()).cellFormula = "SUM(B$colIndex:$lastColumnName$colIndex)"
        }
        workbook.creationHelper.createFormulaEvaluator().evaluateAll()
        for (dep in departments) {
            val departmentRowIndex = findDepartmentRow(sheet, dep.departmentName)
            if (departmentRowIndex == 1) {
                continue
            }
            val depCell = sheet.getRow(departmentRowIndex).getCell(lastCellIndex.toInt())
            depCell.removeFormula()
            depCell.setCellValue("")

        }



        sheet.autoSizeColumn(0)


        FileOutputStream("test.xlsx").use { outputStream -> workbook.write(outputStream) }
        workbook.close()

        val file = File("test.xlsx")
        reportDataDto.fileName = file.name
        println(file.absolutePath)
        model["report_data"] = reportDataDto
        return "admin-panel"
    }
    @Throws(IOException::class)
    @GetMapping("download")
    fun downloadReport(response: HttpServletResponse) {
        val file = File("test.xlsx")
        response.contentType = "application/octet-stream"
        val headerKey: String = "Content-Disposition"
        val headerValue: String = "attachment; filename=" + file.name
        response.setHeader(headerKey, headerValue)
        val outputStream: ServletOutputStream = response.outputStream
        val inputStream: BufferedInputStream = BufferedInputStream(FileInputStream(file))
        val buffer: ByteArray = ByteArray(1024)
        var bytesRead: Int = 0
        while(bytesRead != -1) {
            bytesRead = inputStream.read(buffer)
            outputStream.write(buffer, 0, bytesRead)
        }
        inputStream.close()
        outputStream.close()
    }
}