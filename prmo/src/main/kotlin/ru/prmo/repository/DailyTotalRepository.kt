package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.DailyTotalEntity
import java.time.LocalDate

interface DailyTotalRepository: CrudRepository<DailyTotalEntity, Long> {

    fun findByDate(date: LocalDate): DailyTotalEntity?
}