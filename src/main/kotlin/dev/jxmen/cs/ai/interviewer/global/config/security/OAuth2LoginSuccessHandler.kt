package dev.jxmen.cs.ai.interviewer.global.config.security

import dev.jxmen.cs.ai.interviewer.global.config.security.RefererCaptureFilter.Companion.PREV_PAGE_ATTRIBUTE
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class OAuth2LoginSuccessHandler : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        // NOTE: TokenFilter에서 이미 처리하던 로직을 사용하기 위해 추가
        if (isTokenFilterRequireAuthRequest(request)) {
            return
        }

        // set authentication
        val oidcUser = authentication.principal as OAuth2User
        val token = OAuth2AuthenticationToken(oidcUser, emptyList(), "google")
        SecurityContextHolder.getContext().authentication = token

        // redirect to previous page if exists
        val prevPage = request.session.getAttribute(PREV_PAGE_ATTRIBUTE)
        prevPage?.let {
            request.session.removeAttribute(PREV_PAGE_ATTRIBUTE)
            response.sendRedirect(it as String)
        } ?: response.sendRedirect("/")
    }

    private fun isTokenFilterRequireAuthRequest(request: HttpServletRequest): Boolean {
        val requestUri = request.requestURI
        val matchingEntry = TokenFilter.authRequireUrlMap.entries.find { it.key.matches(requestUri) }

        return matchingEntry != null && matchingEntry.value == HttpMethod.valueOf(request.method)
    }
}
