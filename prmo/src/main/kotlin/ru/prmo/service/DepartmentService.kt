package ru.prmo.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.prmo.entity.DepartmentEntity
import ru.prmo.repository.DepartmentRepository
import java.security.Principal

@Service
class DepartmentService(
    private val departmentRepository: DepartmentRepository,
    @Lazy private val userService: UserService
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
        return getDepartmentById(currentUser.department!!.departmentId)
    }
}