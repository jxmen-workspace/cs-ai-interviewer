package dev.jxmen.cs.ai.interviewer.global.config.service

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MemberCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.member.MemberQueryRepository
import jakarta.servlet.http.HttpSession
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val memberQueryRepository: MemberQueryRepository,
    private val memberCommandRepository: MemberCommandRepository,
    private val httpSession: HttpSession,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    companion object {
        private val defaultOAuth2UserService = DefaultOAuth2UserService()
    }

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        validateGoogleLoginRequest(userRequest) // NOTE: 구글 외 다른 로그인 수단 추가 시 제거
        val oauth2User = defaultOAuth2UserService.loadUser(userRequest)
        val email = oauth2User.attributes["email"].toString()
        val name = oauth2User.attributes["name"].toString()

        // NOTE: 구글 외 다른 로그인 수단 추가 시 아래 로직 변경 필요
        val member = memberQueryRepository.findByEmailOrNull(email)
            ?: memberCommandRepository.save(Member.createGoogleMember(name = name, email = email))
        httpSession.setAttribute("member", member)

        return oauth2User
    }

    private fun validateGoogleLoginRequest(userRequest: OAuth2UserRequest) {
        val registrationId = userRequest.clientRegistration.registrationId
        if (registrationId != "google") {
            throw IllegalArgumentException("Unsupported OAuth2 provider: $registrationId")
        }
    }

    fun MemberQueryRepository.findByEmailOrNull(email: String): Member? = findByEmail(email).orElse(null)
}
