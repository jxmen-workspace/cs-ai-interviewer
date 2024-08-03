package dev.jxmen.cs.ai.interviewer.global.config.security

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.web.client.RestTemplate
import org.springframework.web.filter.OncePerRequestFilter

class TokenFilter : OncePerRequestFilter() {
    val restTemplate = RestTemplate()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authRequireUrls =
            listOf(
                Regex("/api/v2/subjects/\\d+/answer"),
                Regex("/api/v2/chat/messages"),
            )
        if (authRequireUrls.any { it.matches(request.requestURI) }) {
            val token = request.getHeader("Authorization")
            if (token == null || !token.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid or not provided.")
                return
            }

            // NOTE: 구글 외 다른 로그인 수단 추가 시 변경 필요
            val userInfo = fetchGoogleUserInfo(token.substringAfter("Bearer "))
            val oauth2User =
                DefaultOAuth2User(
                    emptyList<GrantedAuthority>(),
                    mapOf(
                        "sub" to userInfo.id,
                        "name" to userInfo.name,
                        "email" to userInfo.email,
                    ),
                    "sub",
                )
            val authentication =
                OAuth2AuthenticationToken(
                    oauth2User,
                    emptyList<GrantedAuthority>(),
                    "google", // NOTE: 구글 외 다른 로그인 수단 추가 시 변경 필요
                )
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun fetchGoogleUserInfo(token: String?): GoogleUserInfo {
        val response =
            restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo?access_token=$token",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                GoogleUserInfo::class.java,
            )

        return response.body ?: throw IllegalArgumentException("Failed to fetch user info.")
    }
}

data class GoogleUserInfo(
    val id: String,
    val email: String,
    @JsonProperty("verified_email")
    val verifiedEmail: Boolean,
    val name: String,
    @JsonProperty("given_name")
    val givenName: String,
    @JsonProperty("family_name")
    val familyName: String,
    val picture: String,
)