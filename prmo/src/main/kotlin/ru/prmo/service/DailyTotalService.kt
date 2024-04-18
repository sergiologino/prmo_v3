package ru.prmo.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.prmo.dto.AdminDailyTotalDto
import ru.prmo.dto.DailyTotalDto
import ru.prmo.dto.OperationRecordDto
import ru.prmo.dto.StringOperationRecordDto
import ru.prmo.entity.*
import ru.prmo.repository.DailyTotalRepository
import ru.prmo.repository.OperationRecordRepository
import ru.prmo.repository.StringOperationRecordRepository
import java.security.Principal
import java.time.LocalDate

@Service
class DailyTotalService(
    private val dailyTotalRepository: DailyTotalRepository,
    private val operationRecordService: OperationRecordService,
    private val operationRecordRepository: OperationRecordRepository,
    private val stringOperationRecordRepository: StringOperationRecordRepository,
    private val userService: UserService,
    private val departmentService: DepartmentService
) {

    fun getAllDailyTotals(): Iterable<AdminDailyTotalDto> {
        return dailyTotalRepository.findAll().map {
            AdminDailyTotalDto(
                date = it.date,
                departmentName = it.department.departmentName,
                total = it.total,
                operationRecords = it.operationRecords.map { opr -> opr.toDto() },
                stringOperationRecords = it.stringOperationRecords.map { opr -> opr.toDto() }
            )
        } //тест
    }

    @Transactional
    fun createDailyTotal(dailyTotalDto: DailyTotalDto, principal: Principal) {
        val currentUser = userService.findByUsername(principal.name)
        val dt = getDailyTotalByDateAndDepartment(dailyTotalDto.date!!, currentUser!!.department!!)

        if (dt!!.operationRecords.isNotEmpty()) {

            dailyTotalRepository.deleteByDateAndDepartment(dailyTotalDto.date!!, currentUser.department!!)
        }

        val notNullCounts = dailyTotalDto.operationRecords.mapNotNull { it.count }
        saveDailyTotalWithOperations(dailyTotalDto, currentUser, department = currentUser.department!!, notNullCounts)


    }

    @Transactional
    fun editDailyTotalByAdmin(dailyTotalDto: DailyTotalDto, principal: Principal, department: DepartmentEntity) {
        val currentUser = userService.findByUsername(principal.name)
        val dt = getDailyTotalByDateAndDepartment(dailyTotalDto.date!!, department)

        if (dt!!.operationRecords.isNotEmpty()) {

            dailyTotalRepository.deleteByDateAndDepartment(dailyTotalDto.date!!, department)
        }

        val notNullCounts = dailyTotalDto.operationRecords.mapNotNull { it.count }
        saveDailyTotalWithOperations(dailyTotalDto, currentUser!!, department, notNullCounts)


    }

    fun getDailyTotalByDateAndDepartment(date: LocalDate, departmentEntity: DepartmentEntity): DailyTotalDto? {
        return dailyTotalRepository.findByDateAndDepartment(date, departmentEntity)?.toDto()
            ?: DailyTotalDto(date = date, departmentName = departmentEntity.departmentName)
    }

    private fun DailyTotalEntity.toDto(): DailyTotalDto {
        return DailyTotalDto(
            date = this.date,
            departmentName = this.department.departmentName,
            operationRecords = this.operationRecords.map {
                OperationRecordDto(
                    operationName = it.operationName,
                    count = it.count
                )
            }.toMutableList(),
            stringOperationRecords = this.stringOperationRecords.map {
                StringOperationRecordDto(
                    operationName = it.operationName,
                    value = it.value
                )
            }.toMutableList()


        )
    }

    fun OperationRecordEntity.toDto(): OperationRecordDto {
        return OperationRecordDto(
            operationName = this.operationName,
            count = this.count
        )
    }

    fun StringOperationRecordEntity.toDto(): StringOperationRecordDto {
        return StringOperationRecordDto(
            operationName = this.operationName,
            value = this.value
        )
    }

    private fun saveDailyTotalWithOperations(
        dailyTotalDto: DailyTotalDto,
        currentUser: UserEntity,
        department: DepartmentEntity,
        notNullCounts: List<Int>
    ) {
        var dailyTotal = DailyTotalEntity(
            date = dailyTotalDto.date!!,
            submittedBy = currentUser.username,
            department = department,
            total = notNullCounts.sum(),
        )
        dailyTotal = dailyTotalRepository.save(dailyTotal)
        val operationRecords = dailyTotalDto.operationRecords.map {
            OperationRecordEntity(
                operationName = it.operationName!!,
                count = it.count,
                dailyTotal = dailyTotal
            )
        }

        val stringOperationRecords = dailyTotalDto.stringOperationRecords.map {
            StringOperationRecordEntity(
                operationName = it.operationName!!,
                value = it.value,
                dailyTotal = dailyTotal
            )
        }

        operationRecordRepository.saveAll(operationRecords)  // не забыть заменить на сервис!!!!
        stringOperationRecordRepository.saveAll(stringOperationRecords)
    }
}