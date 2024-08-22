package dev.jxmen.cs.ai.interviewer.common.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class RefererCaptureFilter : OncePerRequestFilter() {
    companion object {
        const val PREV_PAGE_ATTRIBUTE = "prevPage"

        private const val PREV_PAGE_HEADER = "Referer"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.requestURI.startsWith("/oauth2/authorization")) {
            request.getHeader(PREV_PAGE_HEADER)?.let {
                request.session.setAttribute(PREV_PAGE_ATTRIBUTE, it)
            }
        }

        filterChain.doFilter(request, response)
    }
}
