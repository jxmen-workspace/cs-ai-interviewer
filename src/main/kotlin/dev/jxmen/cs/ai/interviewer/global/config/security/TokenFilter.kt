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
    companion object {
        private val restTemplate = RestTemplate()
        private val authRequireUrlMap =
            mapOf(
                Regex("/api/v2/chat/messages") to HttpMethod.GET,
                Regex("/api/v2/subjects/\\d+/answer") to HttpMethod.POST,
                Regex("/api/v1/subjects/member") to HttpMethod.GET,
                Regex("/api/v1/subjects/\\d+/chats/archive") to HttpMethod.POST,
            )
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (isRequireAuthRequest(request)) {
            val token = extractTokenFromRequest(request)
            if (token == null) {
                logger.warn("Token not found.")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not found.")
                return
            }

            if (!token.startsWith("Bearer ")) {
                logger.warn("Invalid token format.")
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format.")
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

    private fun isRequireAuthRequest(request: HttpServletRequest): Boolean {
        val requestUri = request.requestURI
        val matchingEntry = authRequireUrlMap.entries.find { it.key.matches(requestUri) }

        return matchingEntry != null && matchingEntry.value == HttpMethod.valueOf(request.method)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? =
        if (request.getHeader("Authorization") != null) {
            request.getHeader("Authorization")
        } else if (request.getHeader("authorization") != null) {
            request.getHeader("authorization")
        } else {
            null
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
    val familyName: String?,
    val picture: String,
)
