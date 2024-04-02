package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.OperationRecordEntity

interface OperationRecordRepository: CrudRepository<OperationRecordEntity, Long> {
}