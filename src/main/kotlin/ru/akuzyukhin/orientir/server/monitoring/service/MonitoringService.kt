package ru.akuzyukhin.orientir.server.monitoring.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.common.enum.Importance
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
import ru.akuzyukhin.orientir.server.notification.service.NotificationService
import ru.akuzyukhin.orientir.server.task.entity.TaskExecution
import ru.akuzyukhin.orientir.server.task.repository.TaskExecutionRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository
import java.time.LocalDateTime
import kotlin.math.abs
import java.time.Duration

/**
 * Сервис мониторинга выполнения задач.
 * - отметка выполнения задачи;
 * - осознанный пропуск выполнения задачи;
 * - отметка о невозможности выполнения;
 * - расчет отклонения и определение статуса.
 */
@Service
class MonitoringService(
    private val taskExecutionRepository: TaskExecutionRepository,
    private val wardRepository: WardRepository,
    private val notificationService: NotificationService,
    private val curatorWardRepository: CuratorWardRepository
) {

    /**
     * Отметка выполнения задачи.
     *
     * @param wardUserId идентификатор пользователя-подопечного из JWT
     * @param taskExecutionId идентификатор экземпляра задачи
     * @return данные обновленного экземпляра
     */
    @Transactional
    fun complete(wardUserId: Long, taskExecutionId: Long): Map<String, Any?> {
        val execution = findAndValidateExecution(wardUserId, taskExecutionId)

        val now = LocalDateTime.now()
        val deviationMinutes = Duration.between(execution.scheduledDateTime, now).toMinutes().toInt()
        val windowMinutes = execution.task.windowMinutes
        val isWithinWindow = abs(deviationMinutes) <= windowMinutes

        execution.executionTime = now
        execution.deviationMinutes = deviationMinutes
        execution.isWithinWindow = isWithinWindow
        execution.status = if (isWithinWindow) ExecutionStatus.COMPLETED else ExecutionStatus.COMPLETED_LATE

        taskExecutionRepository.save(execution)
        return buildResponse(execution)
    }


    /**
     * Осознанный пропуск выполнения задачи.
     *
     * @param wardUserId идентификатор пользователя-подопечного из JWT
     * @param taskExecutionId идентификатор экземпляра задачи
     * @return данные обновленного экземпляра
     * @throws IllegalArgumentException если важность не LOW
     */
    @Transactional
    fun skip(wardUserId: Long, taskExecutionId: Long): Map<String, Any?> {
        val execution = findAndValidateExecution(wardUserId, taskExecutionId)

        if (execution.task.importance != Importance.LOW) {
            throw IllegalArgumentException(
                "Пропуск доступен только для задач с низкой важностью. " +
                "Используйте блокировку"
            )
        }

        execution.status = ExecutionStatus.SKIPPED
        taskExecutionRepository.save(execution)
        return buildResponse(execution)
    }

    /**
     *  Блокировка задачи - подопечный сообщает о невозможности выполнения.
     *
     *  @param wardUserId идентификатор пользователя-подопечного из JWT
     *  @param taskExecutionid идентификатор экземпляра задачи
     *  @param comment комментарий подопечного (опционально)
     *  @return данные обновленного экземпляра
     */
    @Transactional
    fun block(wardUserId: Long, taskExecutionId: Long, comment: String?): Map<String, Any?> {
        val execution = findAndValidateExecution(wardUserId, taskExecutionId)

        execution.status = ExecutionStatus.BLOCKED
        taskExecutionRepository.save(execution)

        // Отправка уведомления всем кураторам подопечного
        val ward = wardRepository.findByUserId(wardUserId)!!
        val curatorLinks = curatorWardRepository.findAllByWardId(ward.id)

        for (link in curatorLinks) {
            notificationService.create(
                recipient = link.curator.user,
                type = NotificationType.MISSED,
                title = "Подопечный не может выполнить задачу",
                body = "${ward.user.surname} ${ward.user.name} сообщил о невозможности " +
                        "выполнить задачу «${execution.task.name}»",
                taskExecution = execution,
                comment = comment
            )
        }

        return buildResponse(execution)

    }

    /**
     * Поиск экземпляра задачи с проверкой:
     * - сущестования;
     * - принадлежности подопечности;
     * - статусу "не обработан".
     *
     * @param wardUserId идентификатор пользователя-подопечного из JWT
     * @param taskExecutionid идентификатор экземпляра задачи
     */
    private fun findAndValidateExecution(wardUserId: Long, taskExecutionId: Long): TaskExecution {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")

        val execution = taskExecutionRepository.findById(taskExecutionId)
            .orElseThrow { IllegalArgumentException("Экземпляр задачи не найден") }

        // Проверка принадлежности подопечному
        if (execution.task.schedule.ward.id != ward.id) {
            throw IllegalArgumentException("Задача не принадлежит текущему подопечному")
        }

        // Проверка что задача не является обработанной
        if (execution.status != ExecutionStatus.PENDING) {
            throw IllegalStateException("Задача уже обработана (статус: ${execution.status})")
        }

        return execution
    }

    /**
     * Формирование ответа с данными экземпляра задачи.
     *
     * @param taskExecution экземпляр задачи
     */
    private fun buildResponse(execution: TaskExecution): Map<String, Any?> {
        return mapOf(
            "id" to execution.id,
            "taskId" to execution.task.id,
            "scheduledDatetime" to execution.scheduledDateTime.toString(),
            "executionTime" to execution.executionTime?.toString(),
            "status" to execution.status.name,
            "deviationMinutes" to execution.deviationMinutes,
            "isWithinWindow" to execution.isWithinWindow

        )
    }
}