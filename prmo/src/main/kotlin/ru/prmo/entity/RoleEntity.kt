package ru.prmo.entity

import jakarta.persistence.*

@Entity
@Table(name = "roles")
class RoleEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    val roleId: Long = 0,
    @Column(name = "role_name", unique = true, nullable = false, length = 50)
    val roleName: String = ""
        )