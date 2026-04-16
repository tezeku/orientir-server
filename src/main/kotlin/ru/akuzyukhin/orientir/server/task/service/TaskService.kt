package ru.akuzyukhin.orientir.server.task.service

import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.recur.RecurrenceRule
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.Importance
import ru.akuzyukhin.orientir.server.common.enum.TaskType
import ru.akuzyukhin.orientir.server.schedule.repository.ScheduleRepository
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

/**
 * Сервис управления задачами.
 * - CRUD задач в расписании (куратор);
 * - генерация экземпляров задач на дату (разворачивание RRULE);
 * - просмотр задач подопечным.
 */
@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskExecutionRepository: TaskExecutionRepository,
    private val scheduleRepository: ScheduleRepository,
    private val curatorRepository: CuratorRepository,
    private val wardRepository: WardRepository,
    private val curatorWardRepository: CuratorWardRepository
) {

    /**
     * Создание задачи в расписании.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @param scheduleId идентификатор расписания
     * @param request параметры задачи
     * @return данные созданной задачи
     */
    @Transactional
    fun create(
        curatorUserId: Long,
        wardId: Long,
        scheduleId: Long,
        request: Map<String, String>
    ): Map<String, Any?> {
        validateCuratorWardAccess(curatorUserId, wardId)
        val schedule = findScheduleByIdAndWard(scheduleId, wardId)

        val name = request["name"]
            ?: throw IllegalArgumentException("Название задачи не может быть пустым")
        val type = parseTaskType(request["type"])
        val importance = parseImportance(request["importance"])
        val rrule = request["rrule"]
            ?: throw IllegalArgumentException("Правило повторения не может быть пустым")
        val scheduledTime = parseTime(request["scheduledTime"])
        val windowMinutes = request["windowMinutes"]?.toIntOrNull()
            ?: throw IllegalArgumentException("Временное окно не может быть пустым и должно быть числом")

        // Валидация RRULE
        try {
            RecurrenceRule(rrule)
        } catch (e: Exception) {
            throw IllegalArgumentException("Невалидное правило повторения RRULE: $rrule")
        }

        val task = taskRepository.save(
            Task(
                schedule = schedule,
                name = name,
                type = type,
                importance = importance,
                rrule = rrule,
                scheduledTime = scheduledTime,
                windowMinutes = windowMinutes
            )
        )

        return buildTaskResponse(task)
    }

    /** Получение списка задач расписания (для куратора) */
    fun getAllBySchedule(
        curatorUserId: Long,
        wardId: Long,
        scheduleId: Long
    ): List<Map<String, Any?>> {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)

        return taskRepository.findAllByScheduleId(scheduleId).map { buildTaskResponse(it) }
    }

    /** Получение конкретной задачи (для куратора) */
    fun getOne(
        curatorUserId: Long,
        wardId: Long,
        scheduleId: Long,
        taskId: Long
    ): Map<String, Any?> {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)

        val task = findTaskByIdAndSchedule(taskId, scheduleId)
        return buildTaskResponse(task)
    }

    /** Частичное обновление задачи */
    @Transactional
    fun update(
        curatorUserId: Long,
        wardId: Long,
        scheduleId: Long,
        taskId: Long,
        updates: Map<String, String>
    ): Map<String, Any?> {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)

        val task = findTaskByIdAndSchedule(taskId, scheduleId)

        updates["name"]?.let { task.name = it }
        updates["type"]?.let { task.type = parseTaskType(it) }
        updates["importance"]?.let { task.importance = parseImportance(it) }
        updates["rrule"]?.let { rrule ->
            try {
                RecurrenceRule(rrule)
            } catch(e: Exception) {
                throw IllegalArgumentException("Невалидное правило повторения RRULE: $rrule")
            }
            task.rrule = rrule
        }
        updates["scheduledTime"]?.let { task.scheduledTime = parseTime(it) }
        updates["windowMinutes"]?.let {
            task.windowMinutes = it.toIntOrNull()
                ?: throw IllegalArgumentException("Временное окно должно быть числом")
        }

        taskRepository.save(task)
        return buildTaskResponse(task)
    }

    /** Удаление задачи */
    @Transactional
    fun delete(curatorUserId: Long, wardId: Long, scheduleId: Long, taskId: Long) {
        validateCuratorWardAccess(curatorUserId, wardId)
        findScheduleByIdAndWard(scheduleId, wardId)

        val task = findTaskByIdAndSchedule(taskId, scheduleId)
        taskRepository.delete(task)
    }

    /**
     * Генерация экземпляров задач на дату.
     *
     * @param wardId идентификатор подпоечного
     * @param date дата для генерации
     * @return список экземпляров задач с текущими статусами
     */
    @Transactional
    fun getDailyTasks(wardId: Long, date: LocalDate): List<Map<String, Any?>> {
        val tasks = taskRepository.findAllByScheduleWardId(wardId)
        val dayStart = date.atStartOfDay()
        val dayEnd = date.atTime(23, 59, 59)

        val result = mutableListOf<Map<String, Any?>>()

        for (task in tasks) {
            // Проверка попадания задачи на указанную дату
            if (taskOccursOnDate(task, date)) {
                val scheduleDateTime = LocalDateTime.of(date, task.scheduledTime)

                // Поиск существующего экземпляра или создание нового
                val execution = if (taskExecutionRepository.existsByTaskIdAndScheduledDateTime(
                    task.id, scheduleDateTime
                )) {
                    // Поиск существующего
                    taskExecutionRepository
                        .findAllByTaskIdInAndScheduledDateTimeBetween(
                            listOf(task.id), dayStart, dayEnd
                        )
                        .first { it.scheduledDateTime == scheduleDateTime }
                } else {
                    // Создание нового экземпляра
                    taskExecutionRepository.save(
                        TaskExecution(
                            task = task,
                            scheduledDateTime = scheduleDateTime,
                        )
                    )
                }

                result.add(buildDailyTaskResponse(execution))
            }
        }

        // Сортировка по времени
        return result.sortedBy { it["scheduleDateTime"] as String }
    }

    /** Получение задач на день для куратора */
    fun getDailyTasksForCurator(
        curatorUserId: Long,
        wardId: Long,
        date: LocalDate
    ): List<Map<String, Any?>> {
        validateCuratorWardAccess(curatorUserId, wardId)
        return getDailyTasks(wardId, date)
    }

    /** Получение задач на день для подопечного */
    fun getDailyTasksForWard(wardUserId: Long, date: LocalDate): List<Map<String, Any?>> {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")
        return getDailyTasks(ward.id, date)
    }

    /** Получение конкретной задачи для подопечного */
    fun getTaskForWard(wardUserId: Long, scheduleId: Long, taskId: Long): Map<String, Any?> {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")

        val schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("Расписание не найден") }

        if (schedule.ward.id != ward.id) {
            throw IllegalArgumentException("Расписание не принадлежит подопечному")
        }

        val task = findTaskByIdAndSchedule(taskId, scheduleId)
        return buildTaskResponse(task)
    }

        // Вспомогательные методы:

        /** Проверка попадания задачи на указанную дату по правилу */
    private fun taskOccursOnDate(task: Task, date: LocalDate): Boolean {
        return try {
            val rule = RecurrenceRule(task.rrule)
            val start = DateTime(
                TimeZone.getDefault(),
                date.minusMonths(1).year,
                date.minusMonths(1).monthValue - 1,
                date.minusMonths(1).dayOfMonth,
                task.scheduledTime.hour,
                task.scheduledTime.minute,
                task.scheduledTime.second
            )

            val iterator: RecurrenceRuleIterator = rule.iterator(start)
            val targetTimestamp = DateTime(
                TimeZone.getDefault(),
                date.year,
                date.monthValue - 1,
                date.dayOfMonth,
                task.scheduledTime.hour,
                task.scheduledTime.minute,
                task.scheduledTime.second
            ).timestamp

            // Перебор дат до целевой + 1 день
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
        } catch(e: Exception) {
            false
        }
    }

    /** Проверка доступа куратора к подопечному */
    private fun validateCuratorWardAccess(curatorUserId: Long, wardId: Long) {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        if (!curatorWardRepository.existsByCuratorIdAndWardId(curator.id, wardId)) {
            throw IllegalArgumentException("Куратор не привязан к данному подопечному")
        }
    }

    /** Поиск расписания с проверкой принадлежности подопечному */
    private fun findScheduleByIdAndWard(scheduleId: Long, wardId: Long) =
        scheduleRepository.findById(scheduleId)
            .orElseThrow { IllegalArgumentException("Расписание не найден") }
            .also {
                if (it.ward.id != wardId)
                    throw IllegalArgumentException("Расписание не принадлежит подопечному")
            }

    /** Поиск задачи с проверкой принадлежности расписанию */
    private fun findTaskByIdAndSchedule(taskId: Long, scheduleId: Long): Task {
        val task = taskRepository.findById(taskId)
            .orElseThrow { IllegalArgumentException("Задача не найдена") }

        if (task.schedule.id != scheduleId) {
            throw IllegalArgumentException("Задача не принадлежит данному расписанию")
        }

        return task
    }

    /** Парсинг типа задачи из строки */
    private fun parseTaskType(value: String?): TaskType {
        return try {
            TaskType.valueOf((value ?: throw IllegalArgumentException("Тип задачи не может быть пустым")).uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Невалидный тип задачи: $value. Допустимые: ${TaskType.entries.joinToString()}")
        }
    }

    /** Парсинг важности из строки */
    private fun parseImportance(value: String?): Importance {
        return try {
            Importance.valueOf((value ?: throw IllegalArgumentException("Необходимо указать важность")).uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Невалидная важность: $value. Допустимые: ${Importance.entries.joinToString()}")
        }
    }

    /** Парсинг времени из строки */
    private fun parseTime(value: String?): LocalTime {
        return try {
            LocalTime.parse(value ?: throw IllegalArgumentException("Необходимо указать время выполнения"))
        } catch (e: Exception) {
            throw IllegalArgumentException("Невалидный формат времени: $value. Ожидается HH:MM")
        }
    }

    /** Формирование ответа для задачи-шаблона */
    private fun buildTaskResponse(task: Task): Map<String, Any?> {
        return mapOf(
            "id" to task.id,
            "scheduleId" to task.schedule.id,
            "name" to task.name,
            "type" to task.type.name,
            "importance" to task.importance.name,
            "rrule" to task.rrule,
            "scheduledTime" to task.scheduledTime.toString(),
            "windowMinutes" to task.windowMinutes
        )
    }

    /** Формирование ответа для экземпляра задачи на день */
    private fun buildDailyTaskResponse(execution: TaskExecution): Map<String, Any?> {
        return mapOf(
            "taskExecutionId" to execution.id,
            "task" to mapOf(
                "id" to execution.task.id,
                "name" to execution.task.name,
                "type" to execution.task.type.name,
                "importance" to execution.task.importance.name,
                "windowMinutes" to execution.task.windowMinutes
            ),
            "scheduleName" to execution.task.schedule.name,
            "scheduleDateTime" to execution.scheduledDateTime.toString(),
            "status" to execution.status.name,
            "executionTime" to execution.executionTime?.toString(),
            "deviationMinutes" to execution.deviationMinutes,
            "isWithinWindow" to execution.isWithinWindow
        )
    }
}