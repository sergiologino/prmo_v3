package ru.prmo.repository

import org.springframework.data.repository.CrudRepository
import ru.prmo.entity.RoleEntity

interface RoleRepository: CrudRepository<RoleEntity, Long> {
    fun findByRoleName(roleName: String): RoleEntity
}