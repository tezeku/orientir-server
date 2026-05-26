package ru.akuzyukhin.orientir.server.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table


/**
 * Сущность подопечного - расширение базой сущности [User] для роли WARD.
 *
 * Хранит дополнительные данные, специфичные для подопечного:
 * - адрес проживания (опционально).
 *
 * Связь один-к-одному с [User] через поле user_id.
 */
@Entity
@Table(name = "wards")
class Ward(

    /** Уникальный идентификатор подопечного (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * Внешний ключ на запись о пользователе (один-к-одному).
     * fetch = LAZY - не загружаем User из БД, пока явно не обратимся к нему.
     */
    @OneToOne(fetch = FetchType.LAZY)
    val user: User,

    /** Адрес проживания подопечного (опционально) */
    @Column
    var address: String? = null
)