package ru.prmo.entity

import jakarta.persistence.*

@Entity
@Table(name = "departments")
class DepartmentEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    val departmentId: Long = 0,
    @Column(name = "department_name", unique = true, nullable = false)
    val departmentName: String = "",
    @Column(name = "is_visible", nullable = false)
    val isVisible: Boolean = true,
    @OneToMany(mappedBy = "department")
    val users: List<UserEntity> = emptyList(),
    @ManyToMany
    @JoinTable(
        name = "departments_operations",
        joinColumns = [JoinColumn(name = "department_id")],
        inverseJoinColumns = [JoinColumn(name = "operation_id")]
    )
    val operations: List<OperationEntity> = emptyList(),
    @OneToMany(mappedBy = "department")
    val dailyTotals: List<DailyTotalEntity> = emptyList()
        )