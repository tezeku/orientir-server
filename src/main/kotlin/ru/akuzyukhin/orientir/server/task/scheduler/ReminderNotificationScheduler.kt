package ru.akuzyukhin.orientir.server.task.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
import ru.akuzyukhin.orientir.server.notification.service.NotificationService
import ru.akuzyukhin.orientir.server.task.repository.TaskExecutionRepository
import java.time.LocalDateTime

/**
 * Создаёт напоминания подопечному о задачах, чьё плановое время наступило.
 * Выполняется каждые 60 секунд.
 */
@Component
class ReminderNotificationScheduler(
    private val taskExecutionRepository: TaskExecutionRepository,
    private val notificationService: NotificationService
) {

    @Scheduled(fixedRate = 60_000)
    @Transactional
    fun processReminders() {
        val now = LocalDateTime.now()
        val pendingExecutions = taskExecutionRepository.findAllByStatus(ExecutionStatus.PENDING)

        for (execution in pendingExecutions) {
            if (!now.isBefore(execution.scheduledDateTime)) {
                if (!notificationService.existsReminder(execution)) {
                    val ward = execution.task.schedule.ward
                    notificationService.create(
                        recipient = ward.user,
                        type = NotificationType.REMINDER,
                        title = "Пора выполнить задачу",
                        body = "Наступило время выполнить задачу «${execution.task.name}»",
                        taskExecution = execution
                    )
                }
            }
        }
    }
}
