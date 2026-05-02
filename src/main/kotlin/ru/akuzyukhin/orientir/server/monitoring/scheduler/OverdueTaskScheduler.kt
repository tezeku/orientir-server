package ru.akuzyukhin.orientir.server.monitoring.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
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
    private val curatorWardRepository: CuratorWardRepository
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

        // Поиск всех PENDING экземпляров
        val pendingExecutions = taskExecutionRepository.findAllByStatus(ExecutionStatus.PENDING)

        for (execution in pendingExecutions) {
            val deadline = execution.scheduledDateTime
                .plusMinutes(execution.task.windowMinutes.toLong())

            // Проверка на истечение временного окна
            if (now.isAfter(deadline)) {
                execution.status = ExecutionStatus.OVERDUE
                taskExecutionRepository.save(execution)

                //Уведомление всех кураторов подопечного
                val ward = execution.task.schedule.ward
                val curatorLinks = curatorWardRepository.findAllByWardId(ward.id)

                for (link in curatorLinks) {
                    notificationService.create(
                        recipient = link.curator.user,
                        type = NotificationType.MISSED,
                        title = "Задача просрочена",
                        body = "${ward.user.surname} ${ward.user.name} не выполнил задачу " +
                               "${execution.task.name} (${execution.scheduledDateTime.toLocalDate()})",
                        taskExecution = execution
                    )
                }
            }
        }
    }
}