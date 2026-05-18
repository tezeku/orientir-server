package ru.akuzyukhin.orientir.server.statistics.dto

import java.time.LocalDate

/** Статистика состояния подопечного за период анализа */
data class WardStatisticsResponse(

    val wardId: Long,

    val periodDays: Int,

    val periodFrom: LocalDate,

    val periodTo: LocalDate,

    val totalTasks: Int,

    val completedCount: Int,

    val inWindowCount: Int,

    val overdueCount: Int,

    val blockedCount: Int,

    val skippedCount: Int,

    val pendingCount: Int,

    val completionRatePercent: Int,

    val inWindowRatePercent: Int,

    val overdueRatePercent: Int,

    val blockedRatePercent: Int,

    val avgDeviationMinutes: Int,

    val thresholds: WardThresholdsResponse,

    val breaches: List<ThresholdBreach>
)