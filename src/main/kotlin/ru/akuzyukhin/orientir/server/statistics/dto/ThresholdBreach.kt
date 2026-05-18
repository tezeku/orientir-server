package ru.akuzyukhin.orientir.server.statistics.dto

/** Описание конкретного нарушения порога */
data class ThresholdBreach(
    /** Тип нарушения — для машинной обработки на клиенте */
    val type: BreachType,
    /** Человекочитаемое описание для UI */
    val message: String,
    /** Текущее значение метрики */
    val currentValue: Int,
    /** Установленный куратором порог */
    val thresholdValue: Int
)

enum class BreachType {
    /** Процент выполнения ниже минимально допустимого */
    LOW_COMPLETION,

    /** Процент пропусков превышает допустимый */
    HIGH_OVERDUE,

    /** Среднее отклонение превышает допустимое */
    HIGH_DEVIATION
}