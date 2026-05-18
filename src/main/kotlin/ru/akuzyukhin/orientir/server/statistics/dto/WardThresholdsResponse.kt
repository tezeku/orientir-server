package ru.akuzyukhin.orientir.server.statistics.dto

/** Текущие пороги нарушений для подопечного */
data class WardThresholdsResponse(
    val wardId: Long,
    val minCompletionRatePercent: Int,
    val maxOverdueRatePercent: Int,
    val maxAvgDeviationMinutes: Int,
    val periodDays: Int,
    val maxGlobalDeviationPercent: Int
)