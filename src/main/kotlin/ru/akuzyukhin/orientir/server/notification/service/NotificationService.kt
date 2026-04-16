package ru.akuzyukhin.orientir.server.notification.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
import ru.akuzyukhin.orientir.server.notification.entity.Notification
import ru.akuzyukhin.orientir.server.notification.repository.NotificationRepository
import ru.akuzyukhin.orientir.server.task.entity.TaskExecution
import ru.akuzyukhin.orientir.server.user.entity.User
import ru.akuzyukhin.orientir.server.user.repository.UserRepository
import java.time.LocalDateTime

/**
 * Сервис управления уведомлениями.
 * - создание уведомлений;
 * - получение списка уведомлений пользователя с фильтрами;
 * - отметка уведомлений прочитанными;
 * - подсчет непрочитанных.
 */
@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) {

    /**
     * Создание уведомления.
     *
     * @param recipient получатель уведомления
     * @param type тип уведомления
     * @param title заголовок
     * @param body текст
     * @param taskExecution ссылка на факт выполнения (опционально)
     * @param comment комментарий (опционально)
     * @return созданное уведомление
     */
    @Transactional
    fun create(
        recipient: User,
        type: NotificationType,
        title: String,
        body: String,
        taskExecution: TaskExecution? = null,
        comment: String? = null
    ): Notification {
        val notification = Notification(
            recipient = recipient,
            type = type,
            title = title,
            body = body,
            taskExecution = taskExecution,
            comment = comment,
            sentAt = LocalDateTime.now()
        )
        return notificationRepository.save(notification)
    }

    /**
     * Список уведомлений пользователя с пагинацией.
     *
     * @param userId идентификатор пользователя из JWT
     * @param isRead фильтр по прочтению (опционально)
     * @param page номер страницы
     * @param size размер страницы
     * @return страница уведомлений в формате map
     */
    fun getNotifications(
        userId: Long,
        isRead: Boolean?,
        page: Int,
        size: Int
    ): Map<String, Any?> {
        val pageable = PageRequest.of(page, size)

        val pageResult: Page<Notification> = if (isRead != null) {
            notificationRepository.findAllByRecipientIdAndIsReadOrderBySentAtDesc(
                userId, isRead, pageable
            )
        } else {
            notificationRepository.findAllByRecipientIdOrderBySentAtDesc(userId, pageable)
        }

        return mapOf(
            "content" to pageResult.content.map { buildResponse(it) },
            "page" to pageResult.number,
            "size" to pageResult.size,
            "totalElements" to pageResult.totalElements,
            "totalPages" to pageResult.totalPages
        )
    }

    /**
     * Отметить уведомление прочитанным.
     *
     * @param userId идентификатор пользователя из JWT
     * @param notificationId идентификатор уведомления
     * @param isRead новое значение
     * @return обновленное уведомление
     */
    @Transactional
    fun markAsRead(userId: Long, notificationId: Long, isRead: Boolean): Map<String, Any?> {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Уведомление не найдено") }

        if (notification.recipient.id != userId) {
            throw IllegalArgumentException("Уведомление не принадлежит пользователю")
        }

        notification.isRead = isRead
        notificationRepository.save(notification)

        return buildResponse(notification)
    }

    /**
     * Отметить все уведомления пользователя прочитанными.
     *
     * @param userId идентификтаор пользователя из JWT
     * @return количество обновленных уведомлений
     */
    @Transactional
    fun markAllAsRead(userId: Long): Map<String, Any?> {
        val unread = notificationRepository.findAllByRecipientIdAndIsRead(userId, false)
        unread.forEach { it.isRead = true }
        notificationRepository.saveAll(unread)

        return mapOf("updatedCount" to unread.size)
    }

    /**
     * Количество непрочитанных уведомлений.
     *
     * @param userId идентификатор пользователя из JWT
     * @return счетчик
     */
    fun getUnreadCount(userId: Long): Map<String, Any?> {
        val count = notificationRepository.countByRecipientIdAndIsRead(userId, false)
        return mapOf("unreadCount" to count)
    }

    /**
     * Формирование ответа для уведомления.
     *
     * @param notification уведомление
     */
    private fun buildResponse(notification: Notification): Map<String, Any?> {
        return mapOf(
            "id" to notification.id,
            "type" to notification.type.name,
            "title" to notification.title,
            "body" to notification.body,
            "comment" to notification.comment,
            "taskExecutionId" to notification.taskExecution?.id,
            "sentAt" to notification.sentAt?.toString(),
            "isRead" to notification.isRead
        )
    }
}