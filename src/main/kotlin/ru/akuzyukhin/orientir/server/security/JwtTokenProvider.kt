package ru.akuzyukhin.orientir.server.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

/**
 * Провайдер JWT-токенов.
 *
 * Отвечает за генерацию access/refresh-токенов, их валидацию
 * и извлечение данных из токена.
 *
 * Access-токен - короткоживущий (15 минут) - для авторизации запросов.
 * Refresh-токен - долгоживущий (30 дней), используется для обновления пары токенов.
 */
@Component
class JwtTokenProvider(

    /** Секретный ключ для подписи токенов */
    @Value("\${jwt.secret}")
    private val secret: String,

    /** Время жизни access-токенов в миллисекундах */
    @Value("\${jwt.access-expiration-ms}")
    private val accessExpirationMs: Long,

    /** Время жизни refresh-токена в миллисекундах */
    @Value("\${jwt.refresh-expiration-ms}")
    private val refreshExpirationMs: Long
) {

    /** Формирование криптографического ключа из строки секрета */
    private fun getSigningKey(): SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    /**
     * Генерация access-токена.
     *
     * Записываются userId и role, благодаря чему можно
     * на каждом запросе определить кто обращается и с какой ролью.
     *
     * @param userId идентификатор пользователя
     * @param role роль пользователя
     * @return подписанный JWT-токен
     */
    fun generateAccessToken(userId: Long, role: String): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessExpirationMs))
            .signWith(getSigningKey())
            .compact()

    /**
     * Генерация refresh-токена.
     *
     * Содержит только userId.
     *
     * @param userId идентификатор пользователя
     * @return подписанный JWT-токен
     */
    fun generateRefreshToken(userId: Long): String =
        Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(getSigningKey())
            .compact()

    /**
     * Валидация токена и возврат его содержимого.
     *
     * @param token JWT-токен
     * @return содержимое payload - claims
     */
    fun validateAndGetClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload

    /**
     * Извлечение userId из токена.
     *
     * @param claims содержимое токена
     * @return идентификатор пользователя
     */
    fun getUserId(claims: Claims): Long =
        claims.subject.toLong()

    /**
     * Извлечение роли из токена.
     *
     * @param claims - содержимое токена
     * @return строковое значение роли
     */
    fun getRole(claims: Claims): String =
        claims["role"] as String
}