package ru.akuzyukhin.orientir.server.schedule.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import ru.akuzyukhin.orientir.server.user.entity.Ward

/**
 * Сущность расписания.
 *
 * CRUD: куратор - C, R, U, D; подопечный - R.
 */
@Entity
@Table(name = "schedules")
class Schedule(

    /** Уникальный идентификатор расписания (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** Наименование расписания */
    @Column(nullable = false)
    var name: String,

    /** Внешний ключ на запись подопечного, которому принадлежит расписание */
    @ManyToOne
    @JoinColumn(name = "ward_id", nullable = false)
    val ward: Ward
)