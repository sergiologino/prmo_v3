package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.OperationEntity

interface OperationRepository: CrudRepository<OperationEntity, Long> {

}