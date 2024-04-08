package ru.prmo.service

import org.hibernate.mapping.List
import org.springframework.stereotype.Service
import ru.prmo.dto.AdminDailyTotalDto
import ru.prmo.dto.DailyTotalDto
import ru.prmo.dto.OperationRecordDto
import ru.prmo.entity.DailyTotalEntity
import ru.prmo.entity.DepartmentEntity
import ru.prmo.entity.OperationRecordEntity
import ru.prmo.exception.OneDatasetPerDateException
import ru.prmo.repository.DailyTotalRepository
import ru.prmo.repository.OperationRecordRepository
import java.security.Principal
import java.time.LocalDate

@Service
class DailyTotalService(
    private val dailyTotalRepository: DailyTotalRepository,
    private val operationRecordService: OperationRecordService,
    private val operationRecordRepository: OperationRecordRepository,
    private val userService: UserService,
    private val departmentService: DepartmentService
) {

    fun getAllDailyTotals():Iterable<AdminDailyTotalDto> {
        return dailyTotalRepository.findAll().map {
            AdminDailyTotalDto(
                date = it.date,
                departmentName = it.department.departmentName,
                total = it.total,
                operationRecords = it.operationRecords.map { opr -> opr.toDto() }
            )
        } //тест
    }


    fun createDailyTotal(dailyTotalDto: DailyTotalDto, principal: Principal) {
        val currentUser = userService.findByUsername(principal.name)
        val dt = getDailyTotalByDateAndDepartment(LocalDate.now(), currentUser.department!!)

        if (dt!!.operationRecords.isNotEmpty()) {
            throw OneDatasetPerDateException()
        }

        val notNullCounts = dailyTotalDto.operationRecords.mapNotNull { it.count }

        val dailyTotal = DailyTotalEntity(
            submittedBy = currentUser.username,
            department = departmentService.getDepartmentById(currentUser.department.departmentId),
            total = notNullCounts.sum(),
        )

            val operationRecords = dailyTotalDto.operationRecords.map {
                OperationRecordEntity(
                    operationName = it.operationName!!,
                    count = it.count,
                    dailyTotal = dailyTotalRepository.save(dailyTotal)
                )
            }
            operationRecordRepository.saveAll(operationRecords)  // не забыть заменить на сервис!!!!



    }

//    fun getDailyTotalByDate(date: LocalDate): DailyTotalDto? {
//        return dailyTotalRepository.findByDate(date)?.toDto() ?: DailyTotalDto(
//            date = date
//        )
//    }

    fun getDailyTotalByDateAndDepartment(date: LocalDate, departmentEntity: DepartmentEntity): DailyTotalDto? {
        return dailyTotalRepository.findByDateAndDepartment(date, departmentEntity)?.toDto() ?: DailyTotalDto(date = date)
    }

    private fun DailyTotalEntity.toDto(): DailyTotalDto {
         return DailyTotalDto(
            date = this.date,
            operationRecords = this.operationRecords.map {
                OperationRecordDto(
                    operationName = it.operationName,
                    count = it.count
                )
            }.toMutableList()

        )
//        val dailyTotal = DailyTotalDto()
//        for (operation in operations) {
//            dailyTotal.addRecord(OperationRecordDto(operationName = operation))
//        }
    }

    fun OperationRecordEntity.toDto(): OperationRecordDto {
        return OperationRecordDto(
            operationName = this.operationName,
            count = this.count
        )
    }

}