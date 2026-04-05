package ru.akuzyukhin.orientir.server.user.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import ru.akuzyukhin.orientir.server.common.enum.Role

/**
 * Базовая сущность пользователя системы "Ориентир".
 *
 * Хранит общие для всех ролей данные: ФИО, номер телефона, пароль.
 * Номер телефона уникален и используетс якак логин при аутентификации.
 */
@Entity
@Table(name = "users")
class User(

    /** Уникальный идентификатор пользователя (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** Фамилия пользователя */
    @Column(nullable = false, length = 100)
    var surname: String,

    /** Имя пользователя */
    @Column(length = 100)
    var name: String,

    /** Отчество пользователя (опционально) */
    @Column(length = 100)
    var patronymic: String? = null,

    /** Номер телефона в формате E.164 (уникальный) */
    @Column(name = "phone_number", nullable = false, unique = true, length = 16)
    var phoneNumber: String,

    /** Хэш пароля */
    @Column(nullable = false)
    var password: String,

    /** Роль пользователя - назначается при регистрации и не меняется */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role,

    /** Флаг активности учетной записи */
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
)