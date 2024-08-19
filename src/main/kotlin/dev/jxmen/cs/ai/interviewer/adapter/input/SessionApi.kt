package dev.jxmen.cs.ai.interviewer.adapter.input

import dev.jxmen.cs.ai.interviewer.global.dto.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SessionApi {
    /**
     * 세션 ID 발급 (쿠키에 세션 ID를 저장)
     */
    @GetMapping("/api/v1/session-id")
    @PreAuthorize("permitAll()")
    fun getSessionId(request: HttpServletRequest): ResponseEntity<ApiResponse<Nothing>> {
        request.getSession(true)

        return ResponseEntity.ok(ApiResponse.success())
    }
}
