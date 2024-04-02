package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.DepartmentEntity

interface DepartmentRepository: CrudRepository<DepartmentEntity, Long> {
    fun findByDepartmentName(departmentName: String): DepartmentEntity

    fun findByDepartmentId(departmentId: Long): DepartmentEntity
}