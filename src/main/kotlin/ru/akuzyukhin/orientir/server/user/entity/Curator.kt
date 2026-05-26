package ru.akuzyukhin.orientir.server.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

/**
 * Сущность куратора - расширение базой сущности [User] для роли CURATOR.
 *
 * Хранит дополнительные данные, специфичные для куратора:
 * - email для получения уведомлений.
 *
 * Связь один-к-одному с [User] через поле user_id.
 */
@Entity
@Table(name = "curators")
class Curator(

    /** Уникальный идентификатор куратора (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * Внешний ключ на запись о пользователе (один-к-одному).
     * fetch = LAZY - не загружаем User из БД, пока явно не обратимся к нему.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: User,

    /** Email куратора */
    @Column(nullable = false)
    var email: String
)