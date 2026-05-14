package ru.akuzyukhin.orientir.server.task.service

import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.schedule.repository.ScheduleRepository
import ru.akuzyukhin.orientir.server.task.dto.CreateTaskRequest
import ru.akuzyukhin.orientir.server.task.dto.DailyTaskInfo
import ru.akuzyukhin.orientir.server.task.dto.DailyTaskResponse
import ru.akuzyukhin.orientir.server.task.dto.TaskResponse
import ru.akuzyukhin.orientir.server.task.dto.UpdateTaskRequest
import ru.akuzyukhin.orientir.server.task.entity.Task
import ru.akuzyukhin.orientir.server.task.entity.TaskExecution
import ru.akuzyukhin.orientir.server.task.repository.TaskExecutionRepository
import ru.akuzyukhin.orientir.server.task.repository.TaskRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.TimeZone

/** Сервис управления задачами */
@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskExecutionRepository: TaskExecutionRepository,
    private val scheduleRepository: ScheduleRepository,
    private val curatorRepository: CuratorRepository,
    private val wardRepository: WardRepository,
    private val curatorWardRepository: CuratorWardRepository
) {

    /** Создание задачи в расписании */
    @Transactional
    fun create(
        curatorUserId: Long,
        wardId: Long,
        scheduleId: Long,
        request: CreateTaskRequest
    ): TaskResponse {
        validateCuratorWardAccess(curatorUserId, wardId)
        val schedule = findScheduleByIdAndWard(scheduleId, wardId)

        validateRrule(request.rrule)

        val task = taskRepository.save(
            Task(
                schedule = schedule,
                name = request.name,
                type = request.type,
                importance = request.importance,
                rrule = request.rrule,
                scheduledTime = request.scheduledTime,
                windowMinutes = request.windowMinutes
            )
        )
        return task.toResponse()
    }

    /** Получение списка задач расписания */
    fun getAllBySchedule(curatorUserId: Long, wardId: Long, scheduleId: Long): List<TaskResponse> {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)
        return taskRepository.findAllByScheduleId(scheduleId).map { it.toResponse() }
    }

    /** Получение конкретной задачи */
    fun getOne(curatorUserId: Long, wardId: Long, scheduleId: Long, taskId: Long): TaskResponse {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)
        return findTaskByIdAndSchedule(taskId, scheduleId).toResponse()
    }

    /** Частичное обновление задачи */
    @Transactional
    fun update(
        curatorUserId: Long,
        wardId: Long,
        scheduleId: Long,
        taskId: Long,
        request: UpdateTaskRequest
    ): TaskResponse {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)

        val task = findTaskByIdAndSchedule(taskId, scheduleId)

        request.name?.let { task.name = it }
        request.type?.let { task.type = it }
        request.importance?.let { task.importance = it }
        request.rrule?.let {
            validateRrule(it)
            task.rrule = it
        }
        request.scheduledTime?.let { task.scheduledTime = it }
        request.windowMinutes?.let { task.windowMinutes = it }

        taskRepository.save(task)
        return task.toResponse()
    }

    /** Удаление задачи */
    @Transactional
    fun delete(curatorUserId: Long, wardId: Long, scheduleId: Long, taskId: Long) {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)
        val task = findTaskByIdAndSchedule(taskId, scheduleId)
        taskRepository.delete(task)
    }

    /** Получение задач подопечного на день куратором */
    fun getDailyTasksForCurator(curatorUserId: Long, wardId: Long, date: LocalDate): List<DailyTaskResponse> {
        validateCuratorWardAccess(curatorUserId, wardId)
        return getDailyTasks(wardId, date)
    }

    /** Получение задач на день подопечным */
    fun getDailyTasksForWard(wardUserId: Long, date: LocalDate): List<DailyTaskResponse> {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")
        return getDailyTasks(ward.id, date)
    }

    /** Получение конкретной задачи подопечным */
    fun getTaskForWard(wardUserId: Long, scheduleId: Long, taskId: Long): TaskResponse {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")

        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("Расписание не найдено") }

        if (schedule.ward.id != ward.id) {
            throw IllegalArgumentException("Расписание не принадлежит подопечному")
        }

        return findTaskByIdAndSchedule(taskId, scheduleId).toResponse()
    }

    /** Lazy-генерация экземпляров задач на дату */
    @Transactional
    private fun getDailyTasks(wardId: Long, date: LocalDate): List<DailyTaskResponse> {
        val tasks = taskRepository.findAllByScheduleWardId(wardId)
        val dayStart = date.atStartOfDay()
        val dayEnd = date.atTime(23, 59, 59)
        val result = mutableListOf<DailyTaskResponse>()

        for (task in tasks) {
            if (!taskOccursOnDate(task, date)) continue

            val scheduledDateTime = LocalDateTime.of(date, task.scheduledTime)

            val execution = if (taskExecutionRepository.existsByTaskIdAndScheduledDateTime(
                    task.id, scheduledDateTime
                )) {
                taskExecutionRepository
                    .findAllByTaskIdInAndScheduledDateTimeBetween(
                        listOf(task.id), dayStart, dayEnd
                    )
                    .first { it.scheduledDateTime == scheduledDateTime }
            } else {
                taskExecutionRepository.save(
                    TaskExecution(task = task, scheduledDateTime = scheduledDateTime)
                )
            }

            result.add(execution.toDailyResponse())
        }

        return result.sortedBy { it.scheduledDateTime }
    }

    /** Проверка валидности RRULE */
    private fun validateRrule(rrule: String) {
        try {
            RecurrenceRule(rrule)
        } catch (e: Exception) {
            throw IllegalArgumentException("Невалидное правило повторения RRULE: $rrule")
        }
    }

    /** Проверка попадания задачи на указанную дату по RRULE */
    private fun taskOccursOnDate(task: Task, date: LocalDate): Boolean {
        return try {
            val rule = RecurrenceRule(task.rrule)
            val startDate = date.minusMonths(1)
            val start = DateTime(
                TimeZone.getDefault(),
                startDate.year,
                startDate.monthValue - 1,
                startDate.dayOfMonth,
                task.scheduledTime.hour,
                task.scheduledTime.minute,
                task.scheduledTime.second
            )

            val iterator: RecurrenceRuleIterator = rule.iterator(start)
            val limit = date.plusDays(1).atStartOfDay()
                .atZone(TimeZone.getDefault().toZoneId()).toInstant().toEpochMilli()

            while (iterator.hasNext()) {
                val next = iterator.nextDateTime()
                if (next.timestamp > limit) break
                if (next.year == date.year &&
                    next.month + 1 == date.monthValue &&
                    next.dayOfMonth == date.dayOfMonth
                ) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun validateCuratorWardAccess(curatorUserId: Long, wardId: Long) {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        if (!curatorWardRepository.existsByCuratorIdAndWardId(curator.id, wardId)) {
            throw IllegalArgumentException("Куратор не привязан к данному подопечному")
        }
    }

    private fun findScheduleByIdAndWard(scheduleId: Long, wardId: Long) =
        scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("Расписание не найдено") }
            .also {
                if (it.ward.id != wardId)
                    throw IllegalArgumentException("Расписание не принадлежит подопечному")
            }

    private fun findTaskByIdAndSchedule(taskId: Long, scheduleId: Long): Task {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Задача не найдена") }

        if (task.schedule.id != scheduleId) {
            throw IllegalArgumentException("Задача не принадлежит данному расписанию")
        }
        return task
    }

    /** Маппинг Task в TaskResponse */
    private fun Task.toResponse() = TaskResponse(
        id = id,
        scheduleId = schedule.id,
        name = name,
        type = type,
        importance = importance,
        rrule = rrule,
        scheduledTime = scheduledTime,
        windowMinutes = windowMinutes
    )

    /** Маппинг TaskExecution в DailyTaskResponse. */
    private fun TaskExecution.toDailyResponse() = DailyTaskResponse(
        taskExecutionId = id,
        task = DailyTaskInfo(
            id = task.id,
            name = task.name,
            type = task.type,
            importance = task.importance,
            windowMinutes = task.windowMinutes
        ),
        scheduleName = task.schedule.name,
        scheduledDateTime = scheduledDateTime,
        status = status,
        executionTime = executionTime,
        deviationMinutes = deviationMinutes,
        isWithinWindow = isWithinWindow
    )
}