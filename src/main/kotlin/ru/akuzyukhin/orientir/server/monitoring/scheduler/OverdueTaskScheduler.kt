package ru.akuzyukhin.orientir.server.monitoring.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.common.enum.Importance
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
import ru.akuzyukhin.orientir.server.notification.email.EmailService
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import java.time.LocalDateTime
import ru.akuzyukhin.orientir.server.notification.service.NotificationService
import ru.akuzyukhin.orientir.server.task.repository.TaskExecutionRepository

/**
 * Планировщик автоматической смены статуса просроченных задач.
 *
 * Запускается каждую минуту.
 */
@Component
class OverdueTaskScheduler(
    private val taskExecutionRepository: TaskExecutionRepository,
    private val notificationService: NotificationService,
    private val curatorWardRepository: CuratorWardRepository,
    private val emailService: EmailService
) {

    /**
     * Поиск и обработка просроченных задач.
     *
     * Выполняется каждые 60 секунд
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    fun processOverdueTasks() {
        val now = LocalDateTime.now()

        val pendingExecutions = taskExecutionRepository.findAllByStatus(ExecutionStatus.PENDING)

        for (execution in pendingExecutions) {
            val deadline = execution.scheduledDateTime
                .plusMinutes(execution.task.windowMinutes.toLong())

            if (now.isAfter(deadline)) {
                execution.status = ExecutionStatus.OVERDUE
                taskExecutionRepository.save(execution)

                val ward = execution.task.schedule.ward
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
                        title = "Задача просрочена",
                        body = "$wardFullName не выполнил задачу " +
                               "${execution.task.name} (${execution.scheduledDateTime.toLocalDate()})",
                        taskExecution = execution
                    )

                    if (execution.task.importance == Importance.CRITICAL) {
                        emailService.sendCriticalTaskOverdue(
                            toEmail = link.curator.email,
                            wardName = wardFullName,
                            taskName = execution.task.name,
                            scheduledTime = execution.scheduledDateTime
                        )
                    }

                }
            }
        }
    }
}