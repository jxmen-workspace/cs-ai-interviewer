package dev.jxmen.cs.ai.interviewer.adapter.input

import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IsLoggedInApi {
    @GetMapping("/api/v1/is-logged-in")
    fun isLoggedIn(httpSession: HttpSession): ResponseEntity<IsLoggedInResponse> {
        val isLoggedIn = httpSession.getAttribute("member") != null

        return ResponseEntity.ok(IsLoggedInResponse(isLoggedIn = isLoggedIn))
    }
}

data class IsLoggedInResponse(
    val isLoggedIn: Boolean,
)
