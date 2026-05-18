package ru.akuzyukhin.orientir.server.statistics.entity

import jakarta.persistence.*
import ru.akuzyukhin.orientir.server.user.entity.Ward

/** Настраиваемые пороги нарушений для конкретного подопечного */
@Entity
@Table(name = "ward_thresholds")
class WardThresholds(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false, unique = true)
    val ward: Ward,

    /** Минимально допустимый процент выполнения задач (0..100) */
    @Column(name = "min_completion_rate_percent", nullable = false)
    var minCompletionRatePercent: Int = 70,

    /** Максимально допустимый процент пропусков задач (0..100) */
    @Column(name = "max_overdue_rate_percent", nullable = false)
    var maxOverdueRatePercent: Int = 30,

    /** Максимально допустимое среднее отклонение, в минутах */
    @Column(name = "max_avg_deviation_minutes", nullable = false)
    var maxAvgDeviationMinutes: Int = 30,

    /** Длина окна анализа в днях */
    @Column(name = "period_days", nullable = false)
    var periodDays: Int = 7,

    @Column(name = "max_global_deviation_percent", nullable = false)
    var maxGlobalDeviationPercent: Int = 70
)