package ru.prmo.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val userId: Long = 0,
    @Column(name = "username", unique = true, nullable = false)
    val username: String = "",
    @Column(name = "password", nullable = false, length = 512)
    val password: String = "",
    @Column(name = "is_visible", nullable = false)
    val isVisible: Boolean = true,
    @ManyToOne
    @JoinColumn(name = "department_id")
    val department: DepartmentEntity? = null,
    @ManyToMany
    @JoinTable(
        name = "users_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    val roles: List<RoleEntity> = emptyList()
        )