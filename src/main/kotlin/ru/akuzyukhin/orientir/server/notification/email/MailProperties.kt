package ru.akuzyukhin.orientir.server.notification.email

import org.springframework.boot.context.properties.ConfigurationProperties

/** Свойства email-уведомлений приложения */
@ConfigurationProperties(prefix = "orientir.mail")
data class MailProperties(
    val from: String = "",
    val fromName: String = "Ориентир",
    val enabled: Boolean = true
)