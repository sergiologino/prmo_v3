package ru.prmo.entity

import jakarta.persistence.*

@Entity
@Table(name = "operations")
class OperationEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    val operationId: Long = 0,
    @Column(name = "operation_name", unique = true, nullable = false)
    val operationName: String = "",
    @Column(name = "is_visible", nullable = false)
    val isVisible: Boolean = true
        )