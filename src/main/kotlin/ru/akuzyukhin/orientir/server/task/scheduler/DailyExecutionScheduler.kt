package ru.akuzyukhin.orientir.server.task.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.akuzyukhin.orientir.server.task.service.TaskService
import java.time.LocalDate

/**
 * Планировщик проактивной генерации экземпляров задач (TaskExecution)
 * на текущую дату
 */
@Component
class DailyExecutionScheduler(
    private val taskService: TaskService
) {

    @Scheduled(fixedRate = INTERVAL_MS, initialDelay = INITIAL_DELAY_MS)
    fun generateTodayExecutions() {
        try {
            taskService.ensureDailyExecutions(LocalDate.now())
        } catch (e: Exception) {
            log.error("Failed to generate daily executions", e)
        }
    }

    companion object {
        private const val INTERVAL_MS = 5 * 60 * 1000L
        private const val INITIAL_DELAY_MS = 30 * 1000L
        private val log = LoggerFactory.getLogger(DailyExecutionScheduler::class.java)
    }
}