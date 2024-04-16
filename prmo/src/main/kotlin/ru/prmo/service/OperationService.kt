package ru.prmo.service

import org.springframework.stereotype.Service
import ru.prmo.entity.OperationEntity
import ru.prmo.repository.OperationRepository

@Service
class OperationService(
    private val operationRepository: OperationRepository
) {

    fun getAllOperations(): Iterable<OperationEntity> {
        return operationRepository.findAll()
    }
}