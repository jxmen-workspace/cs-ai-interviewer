package dev.jxmen.cs.ai.interviewer.common.config.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.common.contenttype.ContentType
import dev.jxmen.cs.ai.interviewer.common.RequireLoginApi
import dev.jxmen.cs.ai.interviewer.common.dto.ApiResponse
import dev.jxmen.cs.ai.interviewer.common.enum.ErrorType
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.stereotype.Component
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping

/**
 * @see RequireLoginApi
 */
@Component
class JwtAuthenticationFilter(
    private val tokenManager: TokenManager,
    private val handlerMappings: List<HandlerMapping>,
) : OncePerRequestFilter() {
    companion object {
        private val objectMapper = ObjectMapper()
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // NOTE: 로그인이 필요하지 않은 API라면 Jwt를 검사하지 않고 바로 다음 필터로 넘어감
        if (!requireLogin(request)) {
            filterChain.doFilter(request, response)
            return
        }

        val authorizationHeader = request.getHeader("Authorization")
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            setTokenRequiredErrorResponse(response)
            return
        }

        val accessToken = authorizationHeader.substringAfter("Bearer ")
        try {
            val parsedToken = tokenManager.parseToken(accessToken)
            val oauth2User = createOAuth2User(parsedToken)
            // NOTE: 구글 외 다른 로그인 수단 추가 시 변경 필요
            val authentication = createOAuth2AuthenticationToken(oauth2User = oauth2User, provider = "google")
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
            // Handle token parsing or validation errors
            setInvalidTokenErrorResponse(response)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun createOAuth2AuthenticationToken(
        oauth2User: DefaultOAuth2User,
        provider: String,
    ): OAuth2AuthenticationToken {
        val authentication =
            OAuth2AuthenticationToken(
                oauth2User,
                emptyList<GrantedAuthority>(),
                provider, // NOTE: 구글 외 다른 로그인 수단 추가 시 변경 필요
            )
        return authentication
    }

    private fun createOAuth2User(parsedToken: ParsedToken): DefaultOAuth2User {
        val oauth2User =
            DefaultOAuth2User(
                emptyList<GrantedAuthority>(),
                mapOf(
                    "sub" to parsedToken.memberId,
                    "email" to parsedToken.email,
                ),
                "sub",
            )
        return oauth2User
    }

    private fun setTokenRequiredErrorResponse(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = ContentType.APPLICATION_JSON.toString()
        response.writer.write(
            toJson(
                ApiResponse.failure(ErrorType.TOKEN_REQUIRED.toString(), 401, "Token Required."),
            ),
        )
    }

    private fun setInvalidTokenErrorResponse(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = ContentType.APPLICATION_JSON.toString()
        response.writer.write(
            toJson(
                ApiResponse.failure(ErrorType.INVALID_TOKEN.toString(), 401, "Invalid Token."),
            ),
        )
    }

    private fun toJson(v: Any): String = objectMapper.writeValueAsString(v)

    private fun requireLogin(request: HttpServletRequest): Boolean {
        for (handlerMapping in handlerMappings) {
            val handler =
                try {
                    handlerMapping.getHandler(request) ?: continue
                } catch (e: HttpRequestMethodNotSupportedException) {
                    return false
                }

            if (handler.handler is HandlerMethod) {
                val handlerMethod = handler.handler as HandlerMethod
                if (handlerMethod.getMethodAnnotation(RequireLoginApi::class.java) != null) {
                    return true
                }
            }
        }
        return false
    }
}
