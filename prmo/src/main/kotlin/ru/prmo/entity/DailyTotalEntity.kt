package ru.prmo.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "daily_totals")
class DailyTotalEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "total_id")
    val totalId: Long = 0,
    @Column(name = "date", nullable = false)
    val date: LocalDate = LocalDate.now(),
    @Column(name = "submitted_by", nullable = false)
    val submittedBy: String = "",

    @ManyToOne
    @JoinColumn(name = "department_id")
    val department: DepartmentEntity = DepartmentEntity(),
    @OneToMany(mappedBy = "dailyTotal")
    val operationRecords: List<OperationRecordEntity> = emptyList(),
    @Column(name = "total", nullable = false)
    val total: Int = 0,
        )