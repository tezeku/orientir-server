package ru.akuzyukhin.orientir.server.task.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import java.time.LocalDateTime

/**
 * Сущность факта выполнения задачи.
 *
 * Создается автоматически при разворачивании RRULE на дату.
 * CRUD: подопечный - C, R, U; куратор - R.
 */
@Entity
@Table(name = "task_execution")
class TaskExecution(

    /** Уникальный идентификатор факта выполнения (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** Ссылка на задачу */
    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    val task: Task,

    /** Запланированные дата и время выполнения */
    @Column(name = "scheduled_datetime", nullable = false)
    val scheduledDateTime: LocalDateTime,

    /** Фактическое время выполнения (заполняется при реакции подопечного) */
    @Column(name = "execution_time")
    var executionTime: LocalDateTime? = null,

    /** Статус выполнения */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ExecutionStatus = ExecutionStatus.PENDING,

    /** Отклонение от запланированного времени в минутах */
    @Column(name = "deviation_minutes")
    var deviationMinutes: Int? = null,

    /** Признак попадания в допустимое временное окно */
    @Column(name = "is_within_window")
    var isWithinWindow: Boolean? = null
)