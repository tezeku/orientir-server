package ru.akuzyukhin.orientir.server.task.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.task.entity.Task

/**
 * Репозиторий для работы с таблицей tasks.
 *
 * Предоставляет методы поиска задач по расписанию и подопечному.
 */
interface TaskRepository : JpaRepository<Task, Long> {

    /**
     * Поиск всех задач расписания.
     * Используется куратором при просмотре задач конкретного расписания.
     *
     * @param scheduleId идентификатор расписания
     * @return список задач
     */
    fun findAllByScheduleId(scheduleId: Long): List<Task>

    /**
     * Поиск всех задач всех расписаний подопечного.
     * Используется при генерации экземплров задач на дату.
     *
     * @param wardId идентификатор подопечного
     * @return список задач
     */
    fun findAllByScheduleWardId(wardId: Long): List<Task>
}