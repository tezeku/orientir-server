package ru.akuzyukhin.orientir.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import ru.akuzyukhin.orientir.server.notification.email.MailProperties

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(MailProperties::class)
class ServerApplication

fun main(args: Array<String>) {
	runApplication<ServerApplication>(*args)
}
