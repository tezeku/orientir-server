package ru.akuzyukhin.orientir.server.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT-фильтр аутентификации.
 *
 * Выполняется один раз на каждый HTTP-запрос.
 * Извлекается JWT-токен из заголовка Authorization, валидирует его
 * и устанавливает аутентификацию в SecurityContext.
 *
 * HTTP-запрос -> JWT-фильтр -> SecurityContext -> контроллер.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    /**
     * Основной метод фильтра.
     * - извлечение токена из заголовка Authorization;
     * - валидация токена;
     * - создание объекта аутентификации и передача его в SecurityContext;
     * - передача запроса по цепочке фильтров.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Извлечение токена
            val token = extractToken(request)

            if (token != null) {
                // Валидация токена
                val claims = jwtTokenProvider.validateAndGetClaims(token)
                val userId = jwtTokenProvider.getUserId(claims)
                val role = jwtTokenProvider.getRole(claims)

                // Создание объекта аутентификации
                val authentication = UsernamePasswordAuthenticationToken(
                    userId, null, listOf(SimpleGrantedAuthority("ROLE_$role")))

                // Передача объекта аутентификации в SecurityContext
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            // Обработка невалидного токена
            log.debug("Невалидный JWT", e)
            SecurityContextHolder.clearContext()
        }

        // Передача запроса дальше
        filterChain.doFilter(request, response)
    }

    /**
     * Извлечение JWT-токена из заголовка Authorization.
     *
     * @param request HTTP-запрос
     * @return токен без префикса/null
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization")
        return if (header != null && header.startsWith("Bearer ")) {
            header.substring(7)
        } else {
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    }
}