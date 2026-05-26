package ru.akuzyukhin.orientir.server.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Instant

/** Обработчик случая "запрос пришёл без валидной аутентификации" */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED  // 401
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        val body = mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 401,
            "error" to "Unauthorized",
            "message" to "Требуется авторизация"
        )
        response.writer.write(objectMapper.writeValueAsString(body))
    }
}