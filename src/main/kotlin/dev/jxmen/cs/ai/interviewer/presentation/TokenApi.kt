package dev.jxmen.cs.ai.interviewer.presentation

import dev.jxmen.cs.ai.interviewer.global.config.security.TokenManager
import dev.jxmen.cs.ai.interviewer.global.dto.ApiResponse
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenApi(
    private val tokenManager: TokenManager,
) {
    @GetMapping("/api/v1/token/renew-access-token")
    fun renewAccessToken(
        @Param("refreshToken") refreshToken: String,
    ): ResponseEntity<ApiResponse<String>> {
        val renewedAccessToken = tokenManager.renewAccessToken(refreshToken)

        return ResponseEntity.ok().body(
            ApiResponse.success(
                data = renewedAccessToken,
            ),
        )
    }
}
