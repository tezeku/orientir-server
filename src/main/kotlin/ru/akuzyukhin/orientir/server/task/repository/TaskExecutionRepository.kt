package ru.akuzyukhin.orientir.server.task.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.akuzyukhin.orientir.server.common.enum.ExecutionStatus
import ru.akuzyukhin.orientir.server.task.entity.TaskExecution
import java.time.LocalDateTime

/**
 * Репозиторий для работы с таблицей task_executions.
 *
 * Предоставляет методы поиска фактов выполнения задач.
 */
interface TaskExecutionRepository : JpaRepository<TaskExecution, Long> {

    /**
     * Поиск всех экземпляров задач в диапазоне дат.
     * Используется для отображения задач на день и истории выполнения.
     *
     * @param taskIds список идентификаторов задач
     * @param from начало диапазона
     * @param to конец диапазона
     * @return список фактов выполнения
     */
    fun findAllByTaskIdInAndScheduledDateTimeBetween(
        taskIds: List<Long>,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<TaskExecution>

    /**
     * Проверка существования экземпляра задачи на конкретную дату и время.
     * Используется при генерации, чтобы не создавать дубликаты.
     *
     * @param taskId идентификатор задачи
     * @param scheduledDateTime запланированные дата и время
     * @return true если экземпляр уже существует
     */
    fun existsByTaskIdAndScheduledDateTime(taskId: Long, scheduledDateTime: LocalDateTime): Boolean

    /**
     * Получение всех экземпляров с указанным статусом.
     *
     * @param status статус для поиска
     * @return список экземпляров
     */
    fun findAllByStatus(status: ExecutionStatus): List<TaskExecution>

    /** Идентификаторы всех executions, относящихся к указанной задаче */
    @Query("SELECT te.id FROM TaskExecution te WHERE te.task.id = :taskId")
    fun findIdsByTaskId(@Param("taskId") taskId: Long): List<Long>

    /** Удаление всех executions указанной задачи */
    @Modifying
    @Query("DELETE FROM TaskExecution te WHERE te.task.id = :taskId")
    fun deleteAllByTaskId(@Param("taskId") taskId: Long): Int
}