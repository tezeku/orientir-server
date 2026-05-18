package ru.akuzyukhin.orientir.server.statistics.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

/** Запрос на изменение порогов нарушений */
data class UpdateWardThresholdsRequest(

    @field:Min(0) @field:Max(100)
    val minCompletionRatePercent: Int? = null,

    @field:Min(0) @field:Max(100)
    val maxOverdueRatePercent: Int? = null,

    @field:Min(0)
    val maxAvgDeviationMinutes: Int? = null,

    @field:Min(1) @field:Max(90)
    val periodDays: Int? = null,

    @field:Min(0) @field:Max(100)
    val maxGlobalDeviationPercent: Int? = null
)