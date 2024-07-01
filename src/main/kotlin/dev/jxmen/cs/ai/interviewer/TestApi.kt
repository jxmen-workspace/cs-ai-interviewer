package dev.jxmen.cs.ai.interviewer

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestApi {
    @GetMapping("/api/test/session-id")
    fun testSessionId(httpServletRequest: HttpServletRequest): ResponseEntity<TestSessionIdResponse> {
        val sessionId = httpServletRequest.session.id
        val res = TestSessionIdResponse(sessionId = sessionId)

        return ResponseEntity.ok(res)
    }
}

data class TestSessionIdResponse(
    val sessionId: String,
)
