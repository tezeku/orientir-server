package ru.akuzyukhin.orientir.server.common.enum

/**
 * Типы задач в системе.
 *
 * Определяет смысловую категорию задачи в расписании подопечного.
 */
enum class TaskType {
    /** Прием лекарств */
    MEDICATION,

    /** Физическая активность */
    PHYSICAL,

    /** Социальная активность */
    SOCIAL,

    /** Другое */
    OTHER
}