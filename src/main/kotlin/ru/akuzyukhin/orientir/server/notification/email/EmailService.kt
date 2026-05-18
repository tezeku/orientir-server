package ru.akuzyukhin.orientir.server.notification.email

import jakarta.mail.internet.InternetAddress
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Сервис отправки email-уведомлений */
@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine,
    private val properties: MailProperties
) {
    @Async
    fun sendTest(toEmail: String) {
        val context = Context().apply {
            setVariable("recipientEmail", toEmail)
        }
        val html = templateEngine.process("emails/test", context)
        send(toEmail, "Тестовое письмо от приложения «Ориентир»", html)
    }

    /** Уведомление куратору о блокировке задачи подопечным */
    @Async
    fun sendTaskBlocked(
        toEmail: String,
        wardName: String,
        taskName: String,
        scheduledTime: LocalDateTime,
        comment: String?
    ) {
        val context = Context().apply {
            setVariable("wardName", wardName)
            setVariable("taskName", taskName)
            setVariable("scheduledTime", scheduledTime.format(TIME_FORMATTER))
            setVariable("comment", comment)
        }
        val html = templateEngine.process("emails/task-blocked", context)
        send(toEmail, "Подопечный $wardName не может выполнить задачу «$taskName»", html)
    }

    /** Уведомление куратору о пропуске критичной задачи */
    @Async
    fun sendCriticalTaskOverdue(
        toEmail: String,
        wardName: String,
        taskName: String,
        scheduledTime: LocalDateTime
    ) {
        val context = Context().apply {
            setVariable("wardName", wardName)
            setVariable("taskName", taskName)
            setVariable("scheduledTime", scheduledTime.format(TIME_FORMATTER))
        }
        val html = templateEngine.process("emails/critical-overdue", context)
        send(toEmail, "Пропущена критически важная задача «$taskName»", html)
    }

    /** Уведомление куратору о превышении глобального порога отклонений */
    @Async
    fun sendThresholdBreach(
        toEmail: String,
        wardName: String,
        currentValuePercent: Int,
        thresholdPercent: Int,
        trend: String,
        periodDays: Int
    ) {
        val trendLabel = when (trend) {
            "IMPROVING" -> "улучшение"
            "WORSENING" -> "ухудшение"
            else -> "без существенных изменений"
        }

        val context = Context().apply {
            setVariable("wardName", wardName)
            setVariable("currentValuePercent", currentValuePercent)
            setVariable("thresholdPercent", thresholdPercent)
            setVariable("trendLabel", trendLabel)
            setVariable("periodDays", periodDays)
        }
        val html = templateEngine.process("emails/threshold-breach", context)
        send(
            to = toEmail,
            subject = "Подопечный $wardName: превышен порог отклонений ($currentValuePercent%)",
            html = html
        )
    }

    /** Отправка HTML-письма указанному получателю */
    private fun send(to: String, subject: String, html: String) {
        if (!properties.enabled) {
            log.info("Email отправка отключена флагом, пропуск: to=$to subject=$subject")
            return
        }
        if (to.isBlank()) {
            log.warn("Email пропущен: пустой адрес получателя, subject=$subject")
            return
        }
        try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(InternetAddress(properties.from, properties.fromName, "UTF-8"))
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(html, true)
            mailSender.send(message)
            log.info("Email отправлен: to=$to subject=$subject")
        } catch (e: Exception) {
            log.error("Не удалось отправить email: to=$to subject=$subject", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(EmailService::class.java)
        private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")
    }
}