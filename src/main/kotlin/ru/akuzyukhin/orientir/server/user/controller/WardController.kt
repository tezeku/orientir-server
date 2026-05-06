package ru.akuzyukhin.orientir.server.user.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.akuzyukhin.orientir.server.user.dto.CuratorSummary
import ru.akuzyukhin.orientir.server.user.service.CuratorWardService

@RestController
@RequestMapping("/api/v1/wards/me")
class WardController(
    private val curatorWardService: CuratorWardService
) {

    /**
     * Получение списка кураторов текущего подопечного.
     *
     * @return 200 OK со списком кураторов
     */
    @GetMapping("/curators")
    fun getCurators(authentication: Authentication): ResponseEntity<List<CuratorSummary>> {
        val userId = authentication.principal as Long
        return ResponseEntity.ok(curatorWardService.getCurators(userId))
    }
}