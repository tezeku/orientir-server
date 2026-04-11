package ru.akuzyukhin.orientir.server.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.user.entity.CuratorWard
import ru.akuzyukhin.orientir.server.user.repository.CuratorRepository
import ru.akuzyukhin.orientir.server.user.repository.CuratorWardRepository
import ru.akuzyukhin.orientir.server.user.repository.WardRepository

/**
 * Сервис управления связями куратор-подопечный.
 * - привязка подопечного к куратору по номеру телефона;
 * - получение списка подопечных куратора;
 * - получение списка кураторов подопечного;
 * - удаление связи.
 *
 * CRUD: куратор - C, R, D; подпоечный - R.
 */
@Service
class CuratorWardService(
    private val curatorRepository: CuratorRepository,
    private val wardRepository: WardRepository,
    private val curatorWardRepository: CuratorWardRepository
) {

    /**
     * Привязка подопечного к куратору.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardPhoneNumber номер телефона подопечного
     * @return данные созданной связи
     * @throws IllegalArgumentException если куратор или подопечный не найден
     * @throws IllegalStateException если связь уже существует
     */
    @Transactional
    fun addWard(curatorUserId: Long, wardPhoneNumber: String): Map<String, Any?> {
        // Поиск куратора по userId из JWT
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        // Поиск подопечного по номеру телефона
        val ward = wardRepository.findByUserPhoneNumber(wardPhoneNumber)
            ?: throw IllegalArgumentException("Подопечный с номером телефона $wardPhoneNumber не найден")

        // Проверка отсутствия связи
        if (curatorWardRepository.existsByCuratorIdAndWardId(curator.id, ward.id)) {
            throw IllegalStateException("Подопечный уже привяазан к данному куратору")
        }

        // Создание связи
        val curatorWard = curatorWardRepository.save(
            CuratorWard(curator = curator, ward = ward)
        )
        return buildWardResponse(curatorWard)
    }

    /**
     * Получение списка подопечных куратора
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @return список подопечных с их данными
     */
    fun getWards(curatorUserId: Long): List<Map<String, Any?>> {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        return curatorWardRepository.findAllByCuratorId(curator.id).map { cw ->
            mapOf(
                "id" to cw.ward.id,
                "userId" to cw.ward.user.id,
                "surname" to cw.ward.user.surname,
                "name" to cw.ward.user.name,
                "patronymic" to cw.ward.user.patronymic,
                "phoneNumber" to cw.ward.user.phoneNumber,
                "address" to cw.ward.address
            )
        }
    }

    /**
     * Получение информации о конкретном подопечном.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @return данные подопечного
     * @throws IllegalArgumentException если подопечный не найден или не привязан
     */
    fun getWard(curatorUserId: Long, wardId: Long): Map<String, Any?> {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        val ward = wardRepository.findById(wardId)
            .orElseThrow { IllegalArgumentException("Подопечный не найден") }

        if (!curatorWardRepository.existsByCuratorIdAndWardId(curator.id, ward.id)) {
            throw IllegalArgumentException("Подопечный не привязан к данному куратору")
        }

        return mapOf(
            "id" to ward.id,
            "userId" to ward.user.id,
            "surname" to ward.user.surname,
            "name" to ward.user.name,
            "patronymic" to ward.user.patronymic,
            "phoneNumber" to ward.user.phoneNumber,
            "address" to ward.address
        )
    }

    /**
     * Удаление связи с подопечным.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @throws IllegalArgumentException если связь не найдена
     */
    @Transactional
    fun removeWard(curatorUserId: Long, wardId: Long) {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        val curatorWard = curatorWardRepository.findByCuratorIdAndWardId(curator.id, wardId)
            ?: throw IllegalArgumentException("Связь с подопечным не найдена")

        curatorWardRepository.delete(curatorWard)
    }

    /**
     * Получение списка кураторов подопечного.
     *
     * @param wardUserId идентификатор пользователя-подопечного из JWT
     * @return список кураторов с их данными
     */
    fun getCurators(wardUserId: Long): List<Map<String, Any?>> {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")

        return curatorWardRepository.findAllByWardId(ward.id).map { cw ->
            mapOf(
                "id" to cw.curator.id,
                "userId" to cw.curator.user.id,
                "surname" to cw.curator.user.surname,
                "name" to cw.curator.user.name,
                "patronymic" to cw.curator.user.patronymic,
                "phoneNumber" to cw.curator.user.phoneNumber,
                "email" to cw.curator.email
            )
        }
    }

    /**
     * Формирование ответа при создании связи.
     *
     * @param curatorWard сущность связи куратор-подопечный
     */
    private fun buildWardResponse(curatorWard: CuratorWard): Map<String, Any?> {
        return mapOf(
            "id" to curatorWard.id,
            "curatorId" to curatorWard.curator.id,
            "ward" to mapOf(
                "id" to curatorWard.ward.id,
                "userId" to curatorWard.ward.user.id,
                "surname" to curatorWard.ward.user.surname,
                "name" to curatorWard.ward.user.name,
                "patronymic" to curatorWard.ward.user.patronymic,
                "phoneNumber" to curatorWard.ward.user.phoneNumber,
                "address" to curatorWard.ward.address
            )
        )
    }
}