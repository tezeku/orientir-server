package ru.akuzyukhin.orientir.server.schedule.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.schedule.dto.CreateScheduleRequest
import ru.akuzyukhin.orientir.server.schedule.dto.ScheduleResponse
import ru.akuzyukhin.orientir.server.schedule.dto.UpdateScheduleRequest
import ru.akuzyukhin.orientir.server.schedule.entity.Schedule
import ru.akuzyukhin.orientir.server.schedule.repository.ScheduleRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository

/**
 * Сервис управления расписаниями.
 * - создание расписания для подопечного;
 * - получение списка и конкретного расписания;
 * - обновление и удаление расписания;
 * - просмотр расписаний.
 *
 * Операции записи доступны только куратору.
 */
@Service
class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val curatorRepository: CuratorRepository,
    private val wardRepository: WardRepository,
    private val curatorWardRepository: CuratorWardRepository
) {

    /**
     * Создание расписания для подопечного.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @param name название расписания
     * @return данные созданного расписания
     */
    @Transactional
    fun create(curatorUserId: Long, wardId: Long, request: CreateScheduleRequest): ScheduleResponse {
        validateCuratorWardAccess(curatorUserId, wardId)

        val ward = wardRepository.findById(wardId)
            .orElseThrow { IllegalArgumentException("Подопечный не найден") }

        val schedule = scheduleRepository.save(
            Schedule(name = request.name, ward = ward)
        )
        return schedule.toResponse()
    }

    /**
     * Получение списка расписаний подопечного.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @return список расписаний
     */
    fun getAllByWard(curatorUserId: Long, wardId: Long): List<ScheduleResponse> {
        validateCuratorWardAccess(curatorUserId, wardId)
        return scheduleRepository.findAllByWardId(wardId).map { it.toResponse() }
    }

    /**
     * Получение конкретного расписания
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @return данные расписания
     */
    fun getOne(curatorUserId: Long, wardId: Long, scheduleId: Long): ScheduleResponse {
        validateCuratorWardAccess(curatorUserId, wardId)
        return findScheduleByIdAndWard(scheduleId, wardId).toResponse()
    }

    /** Обновление расписания */
    @Transactional
    fun update(
        curatorUserId: Long,
        wardId: Long,
        scheduleId: Long,
        request: UpdateScheduleRequest
    ): ScheduleResponse {
        validateCuratorWardAccess(curatorUserId, wardId)

        val schedule = findScheduleByIdAndWard(scheduleId, wardId)
        schedule.name = request.name
        scheduleRepository.save(schedule)
        return schedule.toResponse()
    }

    /** Удаление расписания */
    @Transactional
    fun delete(curatorUserId: Long, wardId: Long, scheduleId: Long) {
        validateCuratorWardAccess(curatorUserId, wardId)
        val schedule = findScheduleByIdAndWard(scheduleId, wardId)
        scheduleRepository.delete(schedule)
    }

    /** Получение списка расписаний для подопечного */
    fun getMySchedules(wardUserId: Long): List<ScheduleResponse> {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")
        return scheduleRepository.findAllByWardId(ward.id).map { it.toResponse() }
    }

    /** Получение конкретного расписания подопечным */
    fun getMySchedule(wardUserId: Long, scheduleId: Long): ScheduleResponse {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")
        return findScheduleByIdAndWard(scheduleId, ward.id).toResponse()
    }

    /** Проверка что куратора привязан к подопечному */
    private fun validateCuratorWardAccess(curatorUserId: Long, wardId: Long) {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        if (!curatorWardRepository.existsByCuratorIdAndWardId(curator.id, wardId)) {
            throw IllegalArgumentException("Куратор не привязан к данному подопечному")
        }
    }

    /** Поиск расписания по id с проверкой принадлежности подопечному */
    private fun findScheduleByIdAndWard(scheduleId: Long, wardId: Long): Schedule {
        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("Расписание не найдено") }

        if (schedule.ward.id != wardId) {
            throw IllegalArgumentException("Расписание не принадлежит данному подопечному")
        }
        return schedule
    }

    /** Маппинг Schedule в ScheduleResponse */
    private fun Schedule.toResponse() = ScheduleResponse(
        id = id,
        name = name,
        wardId = ward.id
    )
}