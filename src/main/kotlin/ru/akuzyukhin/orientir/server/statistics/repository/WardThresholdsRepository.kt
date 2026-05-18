package ru.akuzyukhin.orientir.server.statistics.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.statistics.entity.WardThresholds

interface WardThresholdsRepository : JpaRepository<WardThresholds, Long> {

    /** Поиск порогов по идентификатору подопечного */
    fun findByWardId(wardId: Long): WardThresholds?
}