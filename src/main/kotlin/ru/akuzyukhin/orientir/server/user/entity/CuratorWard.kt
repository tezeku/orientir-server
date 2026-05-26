package ru.akuzyukhin.orientir.server.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * Связующая сущность между куратором и подопечным.
 *
 * Обеспечивается как сопровождение нескольких подопечных одним куратором,
 * так и сопровождение одного подопечного несколькими кураторами.
 *
 * Уникальное ограничение на пару (curator_id, ward_id) предотвращает
 * дублирование связей.
 *
 * CRUD: куратор - C, R, D; подопечный - R.
 */
@Entity
@Table(
    name = "curators_wards",
    uniqueConstraints = [UniqueConstraint(columnNames = ["curator_id", "ward_id"])]
)
class CuratorWard(

    /** Уникальный идентификатор связи (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** Внешний ключ на запись о кураторе */
    @ManyToOne
    @JoinColumn(name = "curator_id", nullable = false)
    val curator: Curator,

    /** Внешний ключ на запись о подопечном */
    @ManyToOne
    @JoinColumn(name = "ward_id", nullable = false)
    val ward: Ward
)