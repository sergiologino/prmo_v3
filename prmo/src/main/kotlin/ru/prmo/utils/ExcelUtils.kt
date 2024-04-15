package ru.prmo.utils

import org.apache.poi.xssf.usermodel.XSSFSheet
import ru.prmo.dto.AdminDailyTotalDto
import ru.prmo.dto.OperationRecordDto
import ru.prmo.dto.StringOperationRecordDto
import java.time.LocalDate

fun findDateColumn(sheet: XSSFSheet, date: LocalDate): Int {
    for (column in 1..sheet.getRow(0).lastCellNum) {
        val cell = sheet.getRow(0)?.getCell(column)
        if (cell != null && cell.stringCellValue == date.toString()) {
            return column
        }
    }
    return -1
}

fun findDepartmentRow(sheet: XSSFSheet, departmentName: String): Int {
    for (row in 1..sheet.lastRowNum) {
        val cell = sheet.getRow(row)?.getCell(0)
        if (cell != null && cell.stringCellValue == departmentName) {
            return row
        }
    }
    return -1
}
// можно или нужно сделать из этих двух функций одну


fun findDepartmentOperationRow(sheet: XSSFSheet, dt: AdminDailyTotalDto, oprec: OperationRecordDto): Int {
    val depAreaStart = findDepartmentRow(sheet, dt.departmentName) + 1
    val depAreaEnd = depAreaStart + dt.operationRecords.size + 1
    for (row in depAreaStart..depAreaEnd) {
        val cell = sheet.getRow(row)?.getCell(0)
        if (cell != null && cell.stringCellValue == oprec.operationName) {
            return row
        }
    }

    return -1
}

fun findDepartmentOperationRow(sheet: XSSFSheet, dt: AdminDailyTotalDto, oprec: StringOperationRecordDto): Int {
    val depAreaStart = findDepartmentRow(sheet, dt.departmentName) + 1
    val depAreaEnd = depAreaStart + dt.operationRecords.size + 1
    for (row in depAreaStart..depAreaEnd) {
        val cell = sheet.getRow(row)?.getCell(0)
        if (cell != null && cell.stringCellValue == oprec.operationName) {
            return row
        }
    }

    return -1
}