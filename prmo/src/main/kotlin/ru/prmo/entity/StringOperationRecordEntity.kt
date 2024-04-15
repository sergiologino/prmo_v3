package ru.prmo.entity

import jakarta.persistence.*

@Entity
@Table(name = "string_operation_records")
class StringOperationRecordEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    val recordId: Long = 0,
    @Column(name = "operation_name", nullable = false)
    val operationName: String = "",

    val value: String? = null,
    @ManyToOne
    @JoinColumn(name = "daily_total_id")
    val dailyTotal: DailyTotalEntity = DailyTotalEntity()
)

