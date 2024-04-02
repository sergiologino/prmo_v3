package ru.prmo.entity

import jakarta.persistence.*

@Entity
@Table(name = "operation_records")
class OperationRecordEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    val recordId: Long = 0,
    @Column(name = "operation_name", nullable = false)
    val operationName: String = "",

    val count: Int? = null,
    @ManyToOne
    @JoinColumn(name = "daily_total_id")
    val dailyTotal: DailyTotalEntity = DailyTotalEntity()
        )