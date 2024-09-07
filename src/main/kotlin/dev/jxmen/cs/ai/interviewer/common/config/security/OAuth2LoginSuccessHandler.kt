package dev.jxmen.cs.ai.interviewer.common.config.security

import dev.jxmen.cs.ai.interviewer.common.config.security.RefererCaptureFilter.Companion.PREV_PAGE_ATTRIBUTE
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberCommandRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberQueryRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2LoginSuccessHandler(
    private val tokenManager: TokenManager,
    private val memberQueryRepository: MemberQueryRepository,
    private val memberCommandRepository: MemberCommandRepository,
) : AuthenticationSuccessHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        // set authentication
        val oAuth2User = authentication.principal as OAuth2User
        val attributes = oAuth2User.attributes

        // NOTE: 구글 외 다른 로그인 수단 추가 시 아래 로직 변경 필요
        val member = findOrCreateMember(attributes)
        val token = tokenManager.generateTokens(id = member.id, email = member.email)
        logger.debug("Generated tokens: {}", token)

        // redirect to previous page if exists with tokens
        val prevPage = request.session.getAttribute(PREV_PAGE_ATTRIBUTE)
        prevPage?.let { prevPageUrl ->
            request.session.removeAttribute(PREV_PAGE_ATTRIBUTE)
            response.sendRedirect("$prevPageUrl?accessToken=${token.accessToken}&refreshToken=${token.refreshToken}")
        } ?: response.sendRedirect("/")
    }

    private fun findOrCreateMember(attributes: Map<String, Any>): JpaMember {
        val email = attributes["email"].toString()
        val name = attributes["name"].toString()

        return (
            memberQueryRepository.findByEmailOrNull(email)
                ?: memberCommandRepository.save(JpaMember.createGoogleMember(name, email))
        )
    }

    fun MemberQueryRepository.findByEmailOrNull(email: String): JpaMember? = findByEmail(email).orElse(null)
}
