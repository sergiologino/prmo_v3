package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.StringOperationRecordEntity


interface StringOperationRecordRepository : CrudRepository<StringOperationRecordEntity, Long> {
}