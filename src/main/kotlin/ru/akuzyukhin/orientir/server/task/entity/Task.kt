package ru.akuzyukhin.orientir.server.task.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.akuzyukhin.orientir.server.common.enum.Importance
import ru.akuzyukhin.orientir.server.common.enum.TaskType
import ru.akuzyukhin.orientir.server.schedule.entity.Schedule
import java.time.LocalTime

/**
 * Сущность задачи - атомарная единица расписания.
 *
 * Создается и управляется куратором.
 * CRUD: куратор - C, R, U, D; подопечный - R.
 */
@Entity
@Table(name = "tasks", indexes = [Index(name = "idx_tasks_schedule_id", columnList = "schedule_id")])
class Task(

    /** Уникальный идентификатор задачи (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** Внешний ключ на запись о расписании, которому принадлежит задача */
    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    val schedule: Schedule,

    /** Название задачи */
    @Column(nullable = false)
    var name: String,

    /** Тип задачи */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: TaskType,

    /** Важность задачи */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var importance: Importance,

    /** Правило повторения задачи в формате RRULE */
    @Column(nullable = false)
    var rrule: String,

    /** Запланированное время выполнения */
    @Column(name = "scheduled_time", nullable = false)
    var scheduledTime: LocalTime,

    /** Допустимое отклонение от запланированного времени в минутах */
    @Column(name = "window_minutes", nullable = false)
    var windowMinutes: Int
)