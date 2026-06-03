package ru.akuzyukhin.orientir.server.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

/**
 * Глобальный обработчик исключений.
 *
 * Перехват всех исключений из контроллеров и возврат
 * ответа в едином формате.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Ошибки валидации полей (@Valid).
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return buildResponse(HttpStatus.BAD_REQUEST, message)
    }

    /**
     * Ошибки десериализации запроса (отсутствующие или неверные поля).
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        val message = e.cause?.message ?: "Некорректное тело запроса"
        return buildResponse(HttpStatus.BAD_REQUEST, message)
    }

    /**
     * Ошибки бизнес-логики.
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.BAD_REQUEST, e.message ?: "Некорректный запрос")
    }

    /**
     * Ошибки состояния.
     *
     * @return 403 Forbidden
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(e: IllegalStateException): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.FORBIDDEN, e.message ?: "Доступ запрещен")
    }

    /**
     * Остальные ошибки
     *
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Необработанное исключение в контроллере", e)
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера")
    }

    /** Формирование ответа с ошибкой в едином формате */
    private fun buildResponse(status: HttpStatus, message: String): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(status).body(
            ErrorResponse(
                timestamp = Instant.now().toString(),
                status = status.value(),
                error = status.reasonPhrase,
                message = message
            )
        )
    }

    /**
     * Единый формат ответа с ошибкой.
     *
     * @property timestamp время возникновения ошибки
     * @property status HTTP-код ответа
     * @property error текстовое описание HTTP-статуса
     * @property message детальное сообщение об ошибке
     */
    data class ErrorResponse(
        val timestamp: String,
        val status: Int,
        val error: String,
        val message: String
    )

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }
}