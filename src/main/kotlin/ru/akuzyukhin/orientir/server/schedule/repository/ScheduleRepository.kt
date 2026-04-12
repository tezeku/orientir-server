package ru.akuzyukhin.orientir.server.schedule.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.schedule.entity.Schedule

/**
 * Репозиторий для работы с таблицей schedules.
 *
 * Предоставляет методы поиска расписаний по подопечному
 * для отображения куратору и самому подопечному.
 */
interface ScheduleRepository : JpaRepository<Schedule, Long> {

    /**
     * Получение всех расписаний подопечного.
     *
     * @param wardId идентификатор подопечного
     * @return список расписаний
     */
    fun findAllByWardId(wardId: Long): List<Schedule>
}