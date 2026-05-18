package ru.akuzyukhin.orientir.server.statistics.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.statistics.dto.UpdateWardThresholdsRequest
import ru.akuzyukhin.orientir.server.statistics.dto.WardThresholdsResponse
import ru.akuzyukhin.orientir.server.statistics.entity.WardThresholds
import ru.akuzyukhin.orientir.server.statistics.repository.WardThresholdsRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository

/** Управление порогами нарушений для подопечных */
@Service
class WardThresholdsService(
    private val wardThresholdsRepository: WardThresholdsRepository,
    private val wardRepository: WardRepository,
    private val curatorRepository: CuratorRepository,
    private val curatorWardRepository: CuratorWardRepository
) {
    @Transactional
    fun getOrCreate(curatorUserId: Long, wardId: Long): WardThresholdsResponse {
        validateCuratorWardAccess(curatorUserId, wardId)
        return resolveThresholds(wardId).toResponse()
    }

    @Transactional
    fun update(
        curatorUserId: Long,
        wardId: Long,
        request: UpdateWardThresholdsRequest
    ): WardThresholdsResponse {
        validateCuratorWardAccess(curatorUserId, wardId)

        val thresholds = resolveThresholds(wardId)
        request.minCompletionRatePercent?.let { thresholds.minCompletionRatePercent = it }
        request.maxOverdueRatePercent?.let { thresholds.maxOverdueRatePercent = it }
        request.maxAvgDeviationMinutes?.let { thresholds.maxAvgDeviationMinutes = it }
        request.periodDays?.let { thresholds.periodDays = it }
        request.maxGlobalDeviationPercent?.let { thresholds.maxGlobalDeviationPercent = it }

        return wardThresholdsRepository.save(thresholds).toResponse()
    }

    @Transactional
    fun getForWard(wardId: Long): WardThresholds {
        return resolveThresholds(wardId)
    }

    private fun resolveThresholds(wardId: Long): WardThresholds {
        wardThresholdsRepository.findByWardId(wardId)?.let { return it }

        val ward = wardRepository.findById(wardId)
            .orElseThrow { IllegalArgumentException("Подопечный не найден") }
        return wardThresholdsRepository.save(WardThresholds(ward = ward))
    }

    private fun validateCuratorWardAccess(curatorUserId: Long, wardId: Long) {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        if (!curatorWardRepository.existsByCuratorIdAndWardId(curator.id, wardId)) {
            throw IllegalArgumentException("Куратор не привязан к данному подопечному")
        }
    }

    private fun WardThresholds.toResponse() = WardThresholdsResponse(
        wardId = ward.id,
        minCompletionRatePercent = minCompletionRatePercent,
        maxOverdueRatePercent = maxOverdueRatePercent,
        maxAvgDeviationMinutes = maxAvgDeviationMinutes,
        periodDays = periodDays,
        maxGlobalDeviationPercent = maxGlobalDeviationPercent
    )
}