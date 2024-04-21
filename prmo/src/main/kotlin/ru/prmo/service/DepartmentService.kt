package ru.prmo.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.prmo.dto.DepartmentDto
import ru.prmo.entity.DepartmentEntity
import ru.prmo.repository.DepartmentRepository
import java.security.Principal

@Service
class DepartmentService(
    private val departmentRepository: DepartmentRepository,
    @Lazy private val userService: UserService,
    @Lazy private val operationService: OperationService
) {
    fun getAllDepartments(): Iterable<DepartmentEntity> {
        return departmentRepository.findAll()
    }

    fun getDepartmentByName(departmentName: String): DepartmentEntity {
        return departmentRepository.findByDepartmentName(departmentName)
    }

    fun getDepartmentById(departmentId: Long): DepartmentEntity {
        return departmentRepository.findByDepartmentId(departmentId)
    }

    @Transactional
    fun getDepartmentByUser(principal: Principal): DepartmentEntity {
        val currentUser = userService.findByUsername(principal.name)
        return getDepartmentById(currentUser!!.department!!.departmentId)
    }

    fun addDepartment(departmentDto: DepartmentDto) {
        val operations = operationService.getAllOperations()
        departmentRepository.save(DepartmentEntity(
            departmentName = departmentDto.departmentName,
            operations = operations.toList()
        ))
    }
}