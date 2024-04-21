package ru.prmo.service

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import ru.prmo.dto.OperationDto
import ru.prmo.entity.OperationEntity
import ru.prmo.repository.OperationRepository

@Service
class OperationService(
    private val operationRepository: OperationRepository,
    @Lazy private val departmentService: DepartmentService
) {

    fun getAllOperations(): Iterable<OperationEntity> {
        return operationRepository.findAll()
    }

    fun addOperation(operationDto: OperationDto) {
        val departments = departmentService.getAllDepartments()
        operationRepository.save(OperationEntity(
            operationName = operationDto.operationName,
            departments = departments.toList()
        ))
    }
}