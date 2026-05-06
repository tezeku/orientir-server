package ru.akuzyukhin.orientir.server.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.akuzyukhin.orientir.server.user.dto.CuratorSummary
import ru.akuzyukhin.orientir.server.user.dto.WardSummary
import ru.akuzyukhin.orientir.server.user.entity.Curator
import ru.akuzyukhin.orientir.server.user.entity.CuratorWard
import ru.akuzyukhin.orientir.server.user.entity.Ward
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
    fun addWard(curatorUserId: Long, wardPhoneNumber: String): WardSummary {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        val ward = wardRepository.findByUserPhoneNumber(wardPhoneNumber)
            ?: throw IllegalArgumentException("Подопечный с номером телефона $wardPhoneNumber не найден")

        if (curatorWardRepository.existsByCuratorIdAndWardId(curator.id, ward.id)) {
            throw IllegalStateException("Подопечный уже привязан к данному куратору")
        }

        curatorWardRepository.save(CuratorWard(curator = curator, ward = ward))
        return ward.toSummary()
    }

    /**
     * Получение списка подопечных куратора
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @return список подопечных с их данными
     */
    fun getWards(curatorUserId: Long): List<WardSummary> {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        return curatorWardRepository.findAllByCuratorId(curator.id)
            .map { it.ward.toSummary() }
    }

    /**
     * Получение информации о конкретном подопечном.
     *
     * @param curatorUserId идентификатор пользователя-куратора из JWT
     * @param wardId идентификатор подопечного
     * @return данные подопечного
     * @throws IllegalArgumentException если подопечный не найден или не привязан
     */
    fun getWard(curatorUserId: Long, wardId: Long): WardSummary {
        val curator = curatorRepository.findByUserId(curatorUserId)
            ?: throw IllegalArgumentException("Профиль куратора не найден")

        val ward = wardRepository.findById(wardId)
            .orElseThrow { IllegalArgumentException("Подопечный не найден") }

        if (!curatorWardRepository.existsByCuratorIdAndWardId(curator.id, ward.id)) {
            throw IllegalArgumentException("Подопечный не привязан к данному куратору")
        }

        return ward.toSummary()
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
    fun getCurators(wardUserId: Long): List<CuratorSummary> {
        val ward = wardRepository.findByUserId(wardUserId)
            ?: throw IllegalArgumentException("Профиль подопечного не найден")

        return curatorWardRepository.findAllByWardId(ward.id)
            .map { it.curator.toSummary() }
    }

    private fun Ward.toSummary(): WardSummary = WardSummary(
        id = this.id,
        userId = this.user.id,
        surname = this.user.surname,
        name = this.user.name,
        patronymic = this.user.patronymic,
        phoneNumber = this.user.phoneNumber,
        address = this.address
    )

    private fun Curator.toSummary(): CuratorSummary = CuratorSummary(
        id = this.id,
        userId = this.user.id,
        surname = this.user.surname,
        name = this.user.name,
        patronymic = this.user.patronymic,
        phoneNumber = this.user.phoneNumber,
        email = this.email
    )
}