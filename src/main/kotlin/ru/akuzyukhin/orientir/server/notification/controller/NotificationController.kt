package ru.akuzyukhin.orientir.server.notification.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.notification.service.NotificationService

@RestController
@RequestMapping("/api/v1/users/me/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    /**
     * Список уведомлений с пагинацией.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param isRead фильтр по прочтению (опционально)
     * @param page номер страницы (по умолчанию 0)
     * @param size размер страницы (по умолчанию 20)
     * @return 200 OK со страницей уведомлений
     */
    @GetMapping
    fun getNotifications(
        authentication: Authentication,
        @RequestParam(required = false) isRead: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(notificationService.getNotifications(userId, isRead, page, size))
    }

    /**
     * Отметить уведомление прочитанным.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @param notificationId идентификатор уведомления
     * @param request map с полем isRead
     * @return 200 OK с обновленным уведомлением
     */
    @PatchMapping("/{notificationId}")
    fun markAsRead(
        authentication: Authentication,
        @PathVariable notificationId: Long,
        @RequestBody request: Map<String, Boolean>
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        val isRead = request["isRead"]
            ?: throw IllegalArgumentException("Поле isRead не может быть пустым")
        return ResponseEntity.ok(notificationService.markAsRead(userId, notificationId, isRead))
    }

    /**
     * Отметить все уведомления прочитанными.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @return 200 OK с количеством обновленных
     */
    @PostMapping("/read-all")
    fun markAllAsRead(
        authentication: Authentication
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(notificationService.markAllAsRead(userId))
    }

    /**
     * Количество непрочитанных уведомлений.
     *
     * @param authentication объект аутентификации из SecurityContext
     * @return 200 OK со счетчиком
     */
    @GetMapping("/unread-count")
    fun getUnreadCount(
        authentication: Authentication
    ): ResponseEntity<Map<String, Any?>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(notificationService.getUnreadCount(userId))
    }
}