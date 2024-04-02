package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.UserEntity

interface UserRepository: CrudRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity
}