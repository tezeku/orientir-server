package ru.akuzyukhin.orientir.server.statistics.service

import org.springframework.stereotype.Service
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.task.entity.TaskExecution
import ru.akuzyukhin.orientir.server.task.repository.TaskExecutionRepository
import ru.akuzyukhin.orientir.server.task.repository.TaskRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository
import java.time.LocalDate
import kotlin.collections.filter

/**
 * Сервис статистики и аналитики.
 * - сводка выполнения за период (для куратора и подопечного);
 * - статистика отклонений по типам и важности;
 * - расчет глобального отклонения.
 */
@Service
class StatisticsService(
    private val taskRepository: TaskRepository,
    private val taskExecutionRepository: TaskExecutionRepository,
    private val curatorRepository: CuratorRepository,
    private val wardRepository: WardRepository,
    private val curatorWardRepository: CuratorWardRepository
) {

    /**
     * Получение сводки выполнения за период (для куратора).
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @param from начало периода
     * @param to конец периода
     * @return агрегированная сводка
     */
    fun getSummaryForCurator(
        curatorUserId: Long,
        wardId: Long,
        from: LocalDate,
        to: LocalDate
    ): Map<String, Any?> {
        validateCuratorWardAccess(curatorUserId, wardId)
        return buildSummary(wardId, from, to)
    }

    /**
     * Получение сводки выполнения за период (для подопечного).
     *
     * @param wardUserId идентификатор пользователя-подопечного из JWT
     * @param from начало периода
     * @param to конец периода
     * @return упрощенная сводка
     */
    fun getSummaryForWard(
        wardUserId: Long,
        from: LocalDate,
        to: LocalDate
    ): Map<String, Any?> {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")
        return buildSummary(ward.id, from, to)
    }

    /**
     * Получение статистики отклонений по важности и типу (для куратора).
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @param from начало периода
     * @param to конец периода
     * @return детальная статистика
     */
    fun getDeviations(
        curatorUserId: Long,
        wardId: Long,
        from: LocalDate,
        to: LocalDate
    ): Map<String, Any?> {
        validateCuratorWardAccess(curatorUserId, wardId)
        val executions = getExecutions(wardId, from, to)

        val byImportance = executions.groupBy { it.task.importance.name }.mapValues { (_, execs) ->
            buildGroupStats(execs)
        }

        val byType = executions.groupBy { it.task.type.name }.mapValues { (_, execs) ->
            buildGroupStats(execs)
        }

        return mapOf(
            "wardId" to wardId,
            "period" to mapOf("from" to from.toString(), "to" to to.toString()),
            "byImportance" to byImportance,
            "byType" to byType
        )
    }

    /**
     * Получение глобального отклонения.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @param from начало периода
     * @param to конец периода
     * @return глобальное отклонение с порогом и трендом
     */
    fun getGlobalDeviation(
        curatorUserId: Long,
        wardId: Long,
        from: LocalDate,
        to: LocalDate
    ): Map<String, Any?> {
        validateCuratorWardAccess(curatorUserId, wardId)
        val executions = getExecutions(wardId, from, to)

        if (executions.isEmpty()) {
            return mapOf(
                "wardId" to wardId,
                "period" to mapOf("from" to from.toString(), "to" to to.toString()),
                "globalDeviation" to 0.0,
                "threshold" to 0.7,
                "isExceeded" to false,
                "trend" to "STABLE"
            )
        }

        // Веса важности
        val importanceWeight = mapOf("LOW" to 1.0, "MEDIUM" to 2.0, "CRITICAL" to 3.0)

        // Веса статусов (штрафные баллы)
        val statusWeight = mapOf(
            ExecutionStatus.COMPLETED to 0.0,
            ExecutionStatus.COMPLETED_LATE to 0.3,
            ExecutionStatus.SKIPPED to 0.5,
            ExecutionStatus.BLOCKED to 0.6,
            ExecutionStatus.OVERDUE to 1.0,
            ExecutionStatus.PENDING to 0.0
        )

        var totalWeight = 0.0
        var totalPenalty = 0.0

        for (exec in executions) {
            val iw = importanceWeight[exec.task.importance.name] ?: 1.0
            val sw = statusWeight[exec.status] ?: 0.0
            totalWeight += iw
            totalPenalty += iw * sw
        }

        val globalDeviation = if (totalWeight > 0) totalPenalty / totalWeight else 0.0
        val threshold = 0.7 // TODO: хранить в БД как настройку куратора

        return mapOf(
            "wardId" to wardId,
            "period" to mapOf("from" to from.toString(), "to" to to.toString()),
            "globalDeviation" to Math.round(globalDeviation * 1000.0) / 1000.0,
            "threshold" to threshold,
            "isExceeded" to (globalDeviation >= threshold),
            "trend" to "STABLE" // TODO: сравнение с предыдущим периодом
        )
    }

    // Вспомогательные методы

    /** Получение экземпляров задач подопечного за период */
    private fun getExecutions(wardId: Long, from: LocalDate, to: LocalDate): List<TaskExecution> {
        val tasks = taskRepository.findAllByScheduleWardId(wardId)
        if (tasks.isEmpty()) return emptyList()

        val taskIds = tasks.map { it.id }
        return taskExecutionRepository.findAllByTaskIdInAndScheduledDateTimeBetween(
            taskIds,
            from.atStartOfDay(),
            to.atTime(23, 59, 59)
        )
    }

    /** Формирование сводки выполнения */
    private fun buildSummary(wardId: Long, from: LocalDate, to: LocalDate): Map<String, Any?> {
        val executions = getExecutions(wardId, from, to)
        val total = executions.size
        val byStatus = executions.groupBy { it.status.name }.mapValues { it.value.size }

        val completed = (byStatus["COMPLETED"] ?: 0) + (byStatus["COMPLETED_LATE"] ?: 0)
        val finished = total - (byStatus["PENDING"] ?: 0)

        val completionRate = if (finished > 0) completed.toDouble() / finished else 0.0
        val onTimeRate = if (total > 0) (byStatus["COMPLETED"] ?: 0).toDouble() / total else 0.0

        val completedExecutions = executions.filter {
            it.status == ExecutionStatus.COMPLETED || it.status == ExecutionStatus.COMPLETED_LATE
        }
        val avgDeviation = if (completedExecutions.isNotEmpty()) {
            completedExecutions.mapNotNull { it.deviationMinutes }
                .map { kotlin.math.abs(it) }
                .average()
        } else 0.0

        return mapOf(
            "wardId" to wardId,
            "period" to mapOf("from" to from.toString(), "to" to to.toString()),
            "totalTasks" to total,
            "byStatus" to byStatus,
            "completionRate" to Math.round(completionRate * 1000.0) / 1000.0,
            "onTimeRate" to Math.round(onTimeRate * 1000.0) / 1000.0,
            "averageDeviationMinutes" to Math.round(avgDeviation * 10.0) / 10.0
        )
    }

    /** Статистика по группе экземпляров */
    private fun buildGroupStats(executions: List<TaskExecution>): Map<String, Any?> {
        val byStatus = executions.groupBy { it.status.name }.mapValues { it.value.size }
        val completedExecs = executions.filter {
            it.status == ExecutionStatus.COMPLETED || it.status == ExecutionStatus.COMPLETED_LATE
        }
        val avgDeviation = if (completedExecs.isNotEmpty()) {
            completedExecs.mapNotNull { it.deviationMinutes }
                .map { kotlin.math.abs(it) }
                .average()
        } else 0.0

        return mapOf(
            "total" to executions.size,
            "byStatus" to byStatus,
            "averageDeviationMinutes" to Math.round(avgDeviation * 10.0) / 10.0
        )
    }

    /** Проверка доступа куратора к подопечному */
    private fun validateCuratorWardAccess(curatorUserId: Long, wardId: Long) {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")
        if (!curatorWardRepository.existsByCuratorIdAndWardId(curator.id, wardId)) {
            throw IllegalArgumentException("Куратор не привязан к данному подопечному")
        }
    }
}