package ru.prmo.utils


import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.util.PropertyTemplate
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.prmo.dto.AdminDailyTotalDto
import ru.prmo.dto.ReportDataDto
import ru.prmo.entity.DepartmentEntity
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExcelWriter(
    private val workbook: XSSFWorkbook = XSSFWorkbook(),
    private val sheet: XSSFSheet = workbook.createSheet("ПРМО - Отчет")
) {


    private val font = workbook.createFont()

    init {
        font.fontName = "Arial"
        font.fontHeightInPoints = 10.toShort()
        font.bold = true
    }

    private val depFont = workbook.createFont()

    init {
        depFont.fontName = "Arial"
        depFont.fontHeightInPoints = 10.toShort()
        depFont.bold = true
        depFont.color = IndexedColors.DARK_GREEN.index
    }

    private val headerStyle = workbook.createCellStyle()

    init {
        headerStyle.setFont(font)
        headerStyle.alignment = HorizontalAlignment.CENTER
    }

    private val depStyle = workbook.createCellStyle()

    init {
        depStyle.setFont(depFont)
        depStyle.fillForegroundColor = IndexedColors.BRIGHT_GREEN.index
        depStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        depStyle.alignment = HorizontalAlignment.CENTER
    }

    private val depTotalStyle = workbook.createCellStyle()

    init {
        depTotalStyle.fillForegroundColor = IndexedColors.GOLD.index
        depTotalStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
    }


    private val headerRow = sheet.createRow(0)
    lateinit var startDate: LocalDate
    lateinit var endDate: LocalDate

    fun write(
        departments: List<DepartmentEntity>,
        dts: Iterable<AdminDailyTotalDto>,
        reportDataDto: ReportDataDto
    ): String {
        createHeaderRow(reportDataDto)
        createFirstColumn(departments)
        fillTableWithData(dts)
        createTotalsPart()
        createTotalColumn(departments)
        sheet.autoSizeColumn(0)
        workbook.creationHelper.createFormulaEvaluator().evaluateAll()
        val lastCellIndex = sheet.getRow(0).lastCellNum

        val pt = PropertyTemplate()
        pt.drawBorders(
            CellRangeAddress(0, sheet.lastRowNum, 0, lastCellIndex.toInt()),
            BorderStyle.THIN,
            BorderExtent.ALL
        )
        pt.applyBorders(sheet)

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
        val formattedStartDate = startDate.format(formatter)
        val formattedEndDate = endDate.format(formatter)
        val filePath = "Report$formattedStartDate-$formattedEndDate.xlsx"

        FileOutputStream(filePath).use { outputStream -> workbook.write(outputStream) }
        workbook.close()
        return filePath
    }

    private fun createHeaderRow(reportDataDto: ReportDataDto) {


        val headerCell = headerRow.createCell(0)
        headerCell.setCellValue("Процедура/Дата")
        headerCell.cellStyle = headerStyle
//        val dateStyle = workbook.createCellStyle()
//        dateStyle.dataFormat = createHelper.createDataFormat().getFormat("dd.MM.yyyy")
//         Диапазон дат
//        val startDate = LocalDate.of(2024, 4, 1)
//        val endDate = LocalDate.of(2024, 4, 8)
//        lateinit var startDate: LocalDate
//        lateinit var endDate: LocalDate
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
        sheet.createFreezePane(0, 1)
    }

    private fun createFirstColumn(departments: List<DepartmentEntity>) {

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
    }

    private fun fillTableWithData(dts: Iterable<AdminDailyTotalDto>) {
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
    }

    private fun createTotalsPart() {
        val lastCellIndex = sheet.getRow(0).lastCellNum

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
    }

    private fun createTotalColumn(departments: List<DepartmentEntity>) {
        val lastCellIndex = sheet.getRow(0).lastCellNum
        val lastColumnName = CellReference.convertNumToColString((lastCellIndex - 1))
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
    }
}