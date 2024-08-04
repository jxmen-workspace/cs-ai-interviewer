package dev.jxmen.cs.ai.interviewer.adapter.input

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class IsLoggedInApi {
    @Deprecated("구글 로그인 적용 후 삭제할 예정")
    @GetMapping("/api/v1/is-logged-in")
    fun isLoggedIn(): ResponseEntity<IsLoggedInResponse> = ResponseEntity.ok(IsLoggedInResponse(isLoggedIn = false))
}

data class IsLoggedInResponse(
    val isLoggedIn: Boolean,
)
