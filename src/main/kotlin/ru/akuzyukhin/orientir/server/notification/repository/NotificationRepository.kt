package ru.akuzyukhin.orientir.server.notification.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.notification.entity.Notification

/**
 * Репозиторий для работы с таблицей notifications.
 *
 * Предоставляет методы поиска уведомлений.
 */
interface NotificationRepository : JpaRepository<Notification, Long> {

    /**
     * Уведомления пользователя с пагинацией.
     *
     * @param recipientId идентификатор получателя
     * @param pageable параметры пагинации
     * @return страница уведомлений
     */
    fun findAllByRecipientIdOrderBySentAtDesc(
        recipientId: Long,
        pageable: Pageable
    ): Page<Notification>

    /**
     * Уведомления пользователя с фильтром по прочтению.
     *
     * @param recipientId идентификатор получателя
     * @param isRead признак прочтения
     * @param pageable параметры пагинации
     * @return страница уведомлений
     */
    fun findAllByRecipientIdAndIsReadOrderBySentAtDesc(
        recipientId: Long,
        isRead: Boolean,
        pageable: Pageable
    ): Page<Notification>

    /**
     * Количество непрочитанных уведомлений.
     *
     * @param recipientId идентификатор получателя
     * @param isRead false для подсчета непрочитанных
     * @return количество
     */
    fun countByRecipientIdAndIsRead(recipientId: Long, isRead: Boolean): Long

    /**
     * Все непрочитанные уведомления пользователя (без пагинации).
     *
     * @param recipientId идентификатор получателя
     * @param isRead false
     * @return список непрочитанных
     */
    fun findAllByRecipientIdAndIsRead(recipientId: Long, isRead: Boolean): List<Notification>
}