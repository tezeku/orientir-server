package ru.akuzyukhin.orientir.server.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.user.entity.Curator

/**
 * Репозиторий для работы с таблицей curators.
 *
 * Является расширением базовой сущности пользователя [ru.akuzyukhin.orientir.server.user.entity.User]
 * для пользователей с ролью CURATOR. Хранит дополнительные данные, необходимые для куратора
 * (email для отправки уведомлений).
 */
interface CuratorRepository: JpaRepository<Curator, Long> {

    /**
     * Поиск куратора по идентификатору пользователя.
     * Используется для получения профиля куратора из JWT-токена,
     * в котором хранится userId.
     *
     * @param userId идентификатор записи в таблице users
     * @return найденный куратор/null
     */
    fun findByUserId(userId: Long): Curator?
}