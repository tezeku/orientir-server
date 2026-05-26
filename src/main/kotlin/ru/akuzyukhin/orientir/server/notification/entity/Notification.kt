package ru.akuzyukhin.orientir.server.notification.entity

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
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
import ru.akuzyukhin.orientir.server.task.entity.TaskExecution
import ru.akuzyukhin.orientir.server.user.entity.User
import ru.akuzyukhin.orientir.server.user.entity.Ward
import java.time.LocalDateTime

/**
 * Сущность уведомления.
 *
 * Хранит историю всех сформированных уведомлений системы.
 *
 * CRUD: куратор - C, R, U; подопечный - R, U.
 */
@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_notifications_recipient_id", columnList = "recipient_id"),
        Index(name = "idx_notifications_task_execution_id", columnList = "task_execution_id")
    ]
)
class Notification(

    /** Уникальный идентификатор уведомления (автоинкремент) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** Получатель уведомления */
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    val recipient: User,

    /** Тип уведомления: REMINDER, MISSED, WARNING, MANUAL, THRESHOLD_BREACH */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    /** Ссылка на факт выполнения задачи (опционально - WARNING и MANUAL не привязаны) */
    @ManyToOne
    @JoinColumn(name = "task_execution_id")
    val taskExecution: TaskExecution? = null,

    /** Заголовок уведомления */
    @Column(nullable = false)
    val title: String,

    /** Текст уведомления */
    @Column(nullable = false, columnDefinition = "TEXT")
    val body: String,

    /** Комментарий подопечного при невозможности выполнения (опционально) */
    @Column(columnDefinition = "TEXT")
    val comment: String? = null,

    /** Дата и время отправки */
    @Column(name = "sent_at")
    val sentAt: LocalDateTime? = null,

    /** Признак прочтения получателем */
    @Column(name = "is_read", nullable = false)
    var isRead: Boolean = false
)