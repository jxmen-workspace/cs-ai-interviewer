package dev.jxmen.cs.ai.interviewer.common.config.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.jxmen.cs.ai.interviewer.common.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

class SetCustomResponseAuthenticationEntryPoint : AuthenticationEntryPoint {
    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        // 인증되지 않거나 실패할 경우 공개된 API 외 401 응답
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON.type)
        response.writer.write(
            toJson(
                ErrorResponse(
                    message = authException.message ?: "Unauthorized",
                    status = HttpStatus.UNAUTHORIZED.value(),
                ),
            ),
        )
    }

    private fun toJson(obj: Any): String = objectMapper.writeValueAsString(obj)
}
