package ru.prmo.controller


import jakarta.servlet.ServletOutputStream
import jakarta.servlet.http.HttpServletResponse
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.util.PropertyTemplate
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
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
import ru.prmo.utils.findDateColumn
import ru.prmo.utils.findDepartmentOperationRow
import ru.prmo.utils.findDepartmentRow
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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

        // вынести в отдельный класс
        val workbook = XSSFWorkbook()
        val sheet: XSSFSheet = workbook.createSheet("ПРМО - Отчет")
//        val createHelper = workbook.creationHelper

        val font = workbook.createFont()
        font.fontName = "Arial"
        font.fontHeightInPoints = 10.toShort()
        font.bold = true

        val depFont = workbook.createFont()
        depFont.fontName = "Arial"
        depFont.fontHeightInPoints = 10.toShort()
        depFont.bold = true
        depFont.color = IndexedColors.DARK_GREEN.index


        val headerStyle = workbook.createCellStyle()
        headerStyle.setFont(font)
        headerStyle.alignment = HorizontalAlignment.CENTER

        val headerRow = sheet.createRow(0)
        val headerCell = headerRow.createCell(0)
        headerCell.setCellValue("Процедура/Дата")
        headerCell.cellStyle = headerStyle
//        val dateStyle = workbook.createCellStyle()
//        dateStyle.dataFormat = createHelper.createDataFormat().getFormat("dd.MM.yyyy")
//         Диапазон дат
//        val startDate = LocalDate.of(2024, 4, 1)
//        val endDate = LocalDate.of(2024, 4, 8)
        lateinit var startDate: LocalDate
        lateinit var endDate: LocalDate
        val dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDateFromString = LocalDate.parse(reportDataDto.startDate, dtFormatter)
        val endDateFromString = LocalDate.parse(reportDataDto.endDate, dtFormatter)
        if (reportDataDto.endDate < reportDataDto.startDate) {
            startDate = endDateFromString
            endDate = startDateFromString
        } else {
            startDate = startDateFromString
            endDate = endDateFromString
        }

        var column = 1
        var date = startDate
        while (date.isBefore(endDate.plusDays(1))) {
            val dateHeaderCell = headerRow.createCell(column)
            dateHeaderCell.setCellValue(date.toString())
            dateHeaderCell.cellStyle = headerStyle
            sheet.autoSizeColumn(column)
            column++
            date = date.plusDays(1)
        }

        val depStyle = workbook.createCellStyle()
        depStyle.setFont(depFont)
        depStyle.fillForegroundColor = IndexedColors.BRIGHT_GREEN.index
        depStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        depStyle.alignment = HorizontalAlignment.CENTER

        val depTotalStyle = workbook.createCellStyle()
        depTotalStyle.fillForegroundColor = IndexedColors.GOLD.index
        depTotalStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

        var row = 1
        for (dep in departments) {
            val r = sheet.createRow(row)
            val c = r.createCell(0)
            c.setCellValue(dep.departmentName)
            c.cellStyle = depStyle
            sheet.addMergedRegion(CellRangeAddress(row, row, 0, sheet.getRow(0).lastCellNum.toInt()))
//            sheet.createRow(row).createCell(0).setCellValue(dep.departmentName)
            for (i in 1..dep.operations.size) {
                sheet.createRow(row + i).createCell(0).setCellValue(dep.operations[i - 1].operationName)
            }
            val zhdTotalRow = sheet.createRow(row + dep.operations.size + 1)
            val zhdTotalCell = zhdTotalRow.createCell(0)
            zhdTotalCell.setCellValue("Всего (ЖД)")
            zhdTotalCell.cellStyle = depTotalStyle
            val depTotalRow = sheet.createRow(row + dep.operations.size + 2)
            val depTotalCell = depTotalRow.createCell(0)
            depTotalCell.setCellValue("Всего (Общее количество)")
            depTotalCell.cellStyle = depTotalStyle


            row += (dep.operations.size + 3)
        }



        for (dt in dts) {
            val dateColumnIndex = findDateColumn(sheet, dt.date)
            if (dateColumnIndex != -1) {
                val departmentRowIndex = findDepartmentRow(sheet, dt.departmentName)
                val zhdTotalRow = sheet.getRow(departmentRowIndex + dt.operationRecords.size + 3)
                val zhdTotalCell = zhdTotalRow.createCell(dateColumnIndex)
                val columnName = CellReference.convertNumToColString(dateColumnIndex)
                val start = departmentRowIndex + 1
                val end = departmentRowIndex + 4
                zhdTotalCell.cellFormula = "SUM($columnName$start:$columnName$end)"
                zhdTotalCell.cellStyle = depTotalStyle
                val depTotalRow = sheet.getRow(departmentRowIndex + dt.operationRecords.size + 4)
                val depTotalCell = depTotalRow.createCell(dateColumnIndex)
                depTotalCell.setCellValue(dt.total.toDouble())
                depTotalCell.cellStyle = depTotalStyle
                for (op in dt.operationRecords) {
                    val operationRowIndex = findDepartmentOperationRow(sheet, dt, op)
                    if (operationRowIndex != -1) {
                        sheet.getRow(operationRowIndex).createCell(dateColumnIndex)
                            .setCellValue(op.count?.toDouble() ?: 0.0)
                    }
                    for (strop in dt.stringOperationRecords) {
                        val operationRowIndex = findDepartmentOperationRow(sheet, dt, strop)
                        if (operationRowIndex != -1) {
                            sheet.getRow(operationRowIndex).createCell(dateColumnIndex).setCellValue(strop.value)
                        }
                    }

                }
            }
        }
        val lastCellIndex = sheet.getRow(0).lastCellNum
        val lastColumnName = CellReference.convertNumToColString((lastCellIndex - 1))


        val operationsForTotal = listOf<String>(
            "Отстранения (только РЖД предрейсовые, предсменные)",
            "Выявлены с признаками опьянения (только РЖД послерейсовые, послесменные)",
            "Заболеваний",
            "Направлены к цеховому терапевту",
            "Незавершенные измерения",
            "Повторные измерения",
            "ПРОВЕРКИ",
            "Всего (ЖД)",
            "Всего (общее количество)",
        )
        val totalWithoutFayansovaya = workbook.createCellStyle()
        totalWithoutFayansovaya.fillForegroundColor = IndexedColors.AQUA.index
        totalWithoutFayansovaya.fillPattern = FillPatternType.SOLID_FOREGROUND
        totalWithoutFayansovaya.setFont(font)
        val twfRow = sheet.createRow(sheet.lastRowNum + 1)
        val twfCell = twfRow.createCell(0)
        twfCell.setCellValue("ИТОГИ БЕЗ УЧЁТА Каб. ПРМО Смоленского центра по обслуживанию пассажиров в пригородном сообщении на ст. Фаянсовая")
        twfCell.cellStyle = totalWithoutFayansovaya
        sheet.addMergedRegion(CellRangeAddress(twfRow.rowNum, twfRow.rowNum, 0, sheet.getRow(0).lastCellNum.toInt()))

        val styleForTotal = workbook.createCellStyle()
        styleForTotal.fillForegroundColor = IndexedColors.GOLD.index
        styleForTotal.fillPattern = FillPatternType.SOLID_FOREGROUND

//        var rowForTotal = sheet.lastRowNum + 1
        var startPos = 12
        for (op in operationsForTotal) {
            val r = sheet.createRow(sheet.lastRowNum + 1)
            val c = r.createCell(0)
            c.setCellValue(op)

            for (i in 1 until lastCellIndex.toInt()) {
                val pos1 = startPos
                val pos2 = pos1 + 20
                val pos3 = pos2 + 20
                val pos4 = pos3 + 20
                val pos5 = pos4 + 20
                val colName = CellReference.convertNumToColString(i)
                val totalCell = r.createCell(i)
                totalCell.cellFormula = "SUM($colName$pos1, $colName$pos2, $colName$pos3, $colName$pos4, $colName$pos5)"
                totalCell.cellStyle = styleForTotal

            }
            startPos++
//            rowForTotal++
        }

        val totalWithFayansovaya = workbook.createCellStyle()
        totalWithFayansovaya.fillForegroundColor = IndexedColors.CORAL.index
        totalWithFayansovaya.fillPattern = FillPatternType.SOLID_FOREGROUND
        totalWithFayansovaya.setFont(font)
        val tfRow = sheet.createRow(sheet.lastRowNum + 1)
        val tfCell = tfRow.createCell(0)
        tfCell.setCellValue("ИТОГИ С УЧЁТОМ ВСЕХ ПОДРАЗДЕЛЕНИЙ")
        tfCell.cellStyle = totalWithFayansovaya
        sheet.addMergedRegion(CellRangeAddress(tfRow.rowNum, tfRow.rowNum, 0, sheet.getRow(0).lastCellNum.toInt()))

        var lastTotalStartPos1 = 123
        var lastTotalStartPos2 = 112
        for (op in operationsForTotal) {
            val r = sheet.createRow(sheet.lastRowNum + 1)
            val c = r.createCell(0)
            c.setCellValue(op)

            for (i in 1 until lastCellIndex.toInt()) {

                val colName = CellReference.convertNumToColString(i)
                val totalCell = r.createCell(i)
                totalCell.cellFormula = "SUM($colName$lastTotalStartPos1, $colName$lastTotalStartPos2)"
                totalCell.cellStyle = styleForTotal

            }
            lastTotalStartPos1++
            lastTotalStartPos2++
//            rowForTotal++
        }
        val totalHeaderCell = headerRow.createCell(lastCellIndex.toInt())
        totalHeaderCell.setCellValue("ИТОГО")
        totalHeaderCell.cellStyle = headerStyle

        val totalStyle = workbook.createCellStyle()
        totalStyle.fillForegroundColor = IndexedColors.LIGHT_ORANGE.index
        totalStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

        for (i in 2..sheet.lastRowNum) {
            val colIndex = i + 1
            val r = sheet.getRow(i)
            val c = r.createCell(lastCellIndex.toInt())
            c.cellFormula = "SUM(B$colIndex:$lastColumnName$colIndex)"
            c.cellStyle = totalStyle
        }


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
        workbook.creationHelper.createFormulaEvaluator().evaluateAll()
        val pt = PropertyTemplate()
        pt.drawBorders(
            CellRangeAddress(0, sheet.lastRowNum, 0, lastCellIndex.toInt()),
            BorderStyle.MEDIUM,
            BorderExtent.ALL
        )
        pt.applyBorders(sheet)

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
        val formattedStartDate = startDate.format(formatter)
        val formattedEndDate = endDate.format(formatter)
        val filePath = "reports\\Report $formattedStartDate - $formattedEndDate.xlsx"
        val newDir = File(".", "reports")
        if (!newDir.exists()) {
            newDir.mkdir()
        }
        FileOutputStream(filePath).use { outputStream -> workbook.write(outputStream) }
        workbook.close()

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

        val file = File("reports\\$fileName")
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
}