package ru.akuzyukhin.orientir.server.monitoring.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.common.enum.Importance
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
import ru.akuzyukhin.orientir.server.monitoring.dto.TaskExecutionResponse
import ru.akuzyukhin.orientir.server.notification.email.EmailService
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
    private val curatorWardRepository: CuratorWardRepository,
    private val emailService: EmailService
) {

    /** Отметка выполнения задачи */
    @Transactional
    fun complete(wardUserId: Long, taskExecutionId: Long): TaskExecutionResponse {
        val execution = findAndValidateExecution(wardUserId, taskExecutionId)

        val now = LocalDateTime.now()
        val deviationMinutes = Duration.between(execution.scheduledDateTime, now).toMinutes().toInt()
        val isWithinWindow = abs(deviationMinutes) <= execution.task.windowMinutes

        execution.executionTime = now
        execution.deviationMinutes = deviationMinutes
        execution.isWithinWindow = isWithinWindow
        execution.status = if (isWithinWindow) ExecutionStatus.COMPLETED else ExecutionStatus.COMPLETED_LATE

        taskExecutionRepository.save(execution)
        return execution.toResponse()
    }

    /** Осознанный пропуск выполнения задачи */
    @Transactional
    fun skip(wardUserId: Long, taskExecutionId: Long): TaskExecutionResponse {
        val execution = findAndValidateExecution(wardUserId, taskExecutionId)

        if (execution.task.importance != Importance.LOW) {
            throw IllegalArgumentException(
                "Пропуск доступен только для задач с низкой важностью. Используйте блокировку."
            )
        }

        execution.status = ExecutionStatus.SKIPPED
        taskExecutionRepository.save(execution)
        return execution.toResponse()
    }

    /** Блокировка задачи - подопечный сообщает о невозможности выполнения */
    @Transactional
    fun block(wardUserId: Long, taskExecutionId: Long, comment: String?): TaskExecutionResponse {
        val execution = findAndValidateExecution(wardUserId, taskExecutionId)

        execution.status = ExecutionStatus.BLOCKED
        taskExecutionRepository.save(execution)

        val ward = requireNotNull(wardRepository.findByUserId(wardUserId)) {
            "Профиль подопечного не найден"
        }
        val curatorLinks = curatorWardRepository.findAllByWardId(ward.id)

        val wardFullName = listOfNotNull(
            ward.user.surname,
            ward.user.name,
            ward.user.patronymic
        ).joinToString(" ")

        for (link in curatorLinks) {
            notificationService.create(
                recipient = link.curator.user,
                type = NotificationType.MISSED,
                title = "Подопечный не может выполнить задачу",
                body = "$wardFullName сообщил о невозможности " +
                        "выполнить задачу «${execution.task.name}»",
                taskExecution = execution,
                comment = comment
            )

            emailService.sendTaskBlocked(
                toEmail = link.curator.email,
                wardName = wardFullName,
                taskName = execution.task.name,
                scheduledTime = execution.scheduledDateTime,
                comment = comment
            )
        }

        return execution.toResponse()
    }

    /**
     * Поиск экземпляра задачи с проверкой:
     * - существования;
     * - принадлежности подопечному;
     * - статуса "не обработан".
     * */
    private fun findAndValidateExecution(wardUserId: Long, taskExecutionId: Long): TaskExecution {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")

        val execution = taskExecutionRepository.findById(taskExecutionId)
            .orElseThrow { IllegalArgumentException("Экземпляр задачи не найден") }

        if (execution.task.schedule.ward.id != ward.id) {
            throw IllegalArgumentException("Задача не принадлежит текущему подопечному")
        }

        if (execution.status != ExecutionStatus.PENDING) {
            throw IllegalStateException("Задача уже обработана (статус: ${execution.status})")
        }

        return execution
    }

    /** Маппинг TaskExecution в TaskExecutionResponse */
    private fun TaskExecution.toResponse() = TaskExecutionResponse(
        id = id,
        taskId = task.id,
        scheduledDateTime = scheduledDateTime,
        executionTime = executionTime,
        status = status,
        deviationMinutes = deviationMinutes,
        isWithinWindow = isWithinWindow
    )

}