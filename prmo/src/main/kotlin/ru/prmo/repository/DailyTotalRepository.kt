package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.DailyTotalEntity
import ru.prmo.entity.DepartmentEntity
import java.time.LocalDate

interface DailyTotalRepository : CrudRepository<DailyTotalEntity, Long> {

    fun findByDate(date: LocalDate): DailyTotalEntity?

    fun findByDateAndDepartment(date: LocalDate, departmentEntity: DepartmentEntity): DailyTotalEntity?

    fun deleteByDateAndDepartment(date: LocalDate, departmentEntity: DepartmentEntity)
}