package ru.akuzyukhin.orientir.server.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.akuzyukhin.orientir.server.user.entity.CuratorWard

/**
 * Репозиторий для работы с таблицей curators_wards
 *
 * Реализует связь многие-ко-многим между кураторами и подопечными.
 * Обеспечивается как сопровождение нескольких подопечных одним куратором,
 * так и сопровождение одного подопечного несколькими кураторами.
 *
 * CRUD: куратор - C, R, D; подопечный - R.
 */
interface CuratorWardRepository : JpaRepository<CuratorWard, Long> {

    /**
     * Получение всех связей куратора для списка подопечных.
     *
     * @param curatorId идентификатор куратора
     * @return список связей куратора с подопечными
     */
    fun findAllByCuratorId(curatorId: Long): List<CuratorWard>

    /**
     * Получение всех связей подопечного для списка кураторов
     *
     * @param wardId идентификатор подопечного
     * @return список связей подопечного с кураторами
     */
    fun findAllByWardId(wardId: Long): List<CuratorWard>

    /**
     * Проверка существования связи между куратором и подопечным
     * Используется:
     * - предотвращение дубликата при создании связи;
     * - проверка прав доступа.
     *
     * @param curatorId идентификатор куратора
     * @param wardId идентификатор подопечного
     * @return true - связь существует
     */
    fun existsByCuratorIdAndWardId(curatorId: Long, wardId: Long): Boolean

    /**
     * Поиск конкретной связи для удаления.
     *
     * @param curatorId идентификатор куратора
     * @param wardId идентификатор подопечного
     * @return найденная связь/null
     */
    fun findByCuratorIdAndWardId(curatorId: Long, wardId: Long): CuratorWard?
}