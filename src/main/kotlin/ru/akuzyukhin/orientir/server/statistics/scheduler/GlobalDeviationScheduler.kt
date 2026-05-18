package ru.akuzyukhin.orientir.server.statistics.scheduler

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.common.enum.NotificationType
import ru.akuzyukhin.orientir.server.notification.email.EmailService
import ru.akuzyukhin.orientir.server.notification.service.NotificationService
import ru.akuzyukhin.orientir.server.statistics.service.StatisticsService
import ru.akuzyukhin.orientir.server.statistics.service.WardThresholdsService
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository
import java.time.LocalDate

/** Планировщик автоматической проверки глобального коэффициента отклонений */
@Component
class GlobalDeviationScheduler(
    private val wardRepository: WardRepository,
    private val curatorWardRepository: CuratorWardRepository,
    private val wardThresholdsService: WardThresholdsService,
    private val statisticsService: StatisticsService,
    private val notificationService: NotificationService,
    private val emailService: EmailService
) {

    @Scheduled(cron = SCHEDULE_CRON)
    @Transactional
    fun checkAllWards() {
        log.info("GlobalDeviationScheduler: starting daily threshold check")

        val allWards = wardRepository.findAll()
        var breachCount = 0

        for (ward in allWards) {
            try {
                if (checkWardAndNotify(ward.id)) breachCount++
            } catch (e: Exception) {
                log.error("Failed to check ward id=${ward.id}", e)
            }
        }

        log.info("GlobalDeviationScheduler: done, breaches detected = $breachCount of ${allWards.size}")
    }

    private fun checkWardAndNotify(wardId: Long): Boolean {
        val thresholds = wardThresholdsService.getForWard(wardId)

        val to = LocalDate.now()
        val from = to.minusDays((thresholds.periodDays - 1).toLong())

        val result = statisticsService.calculateGlobalDeviationForScheduler(wardId, from, to)

        val currentG = result["globalDeviation"] as Double
        val threshold = result["threshold"] as Double
        val isExceeded = result["isExceeded"] as Boolean
        val trend = result["trend"] as String

        if (!isExceeded) {
            log.debug("Ward id=$wardId: G=$currentG, T=$threshold, ok")
            return false
        }

        log.info("Ward id=$wardId: BREACH detected G=$currentG >= T=$threshold trend=$trend")

        val ward = wardRepository.findById(wardId).get()
        val wardFullName = listOfNotNull(
            ward.user.surname,
            ward.user.name,
            ward.user.patronymic
        ).joinToString(" ")

        val curatorLinks = curatorWardRepository.findAllByWardId(wardId)
        val currentPercent = (currentG * 100).toInt()
        val thresholdPercent = thresholds.maxGlobalDeviationPercent

        for (link in curatorLinks) {
            notificationService.create(
                recipient = link.curator.user,
                type = NotificationType.THRESHOLD_BREACH,
                title = "Превышен порог отклонений",
                body = "$wardFullName: коэффициент отклонений достиг $currentPercent% " +
                        "(норма до $thresholdPercent%)",
                taskExecution = null,
                comment = null
            )

            emailService.sendThresholdBreach(
                toEmail = link.curator.email,
                wardName = wardFullName,
                currentValuePercent = currentPercent,
                thresholdPercent = thresholdPercent,
                trend = trend,
                periodDays = thresholds.periodDays
            )
        }

        return true
    }

    companion object {
        private const val SCHEDULE_CRON = "0 0 9 * * *"

        private val log = LoggerFactory.getLogger(GlobalDeviationScheduler::class.java)
    }
}