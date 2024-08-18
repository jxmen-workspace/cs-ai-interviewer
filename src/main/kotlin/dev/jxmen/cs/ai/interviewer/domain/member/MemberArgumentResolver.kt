package dev.jxmen.cs.ai.interviewer.domain.member

import dev.jxmen.cs.ai.interviewer.domain.member.exceptions.UnAuthorizedException
import dev.jxmen.cs.ai.interviewer.global.enum.ErrorType
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class MemberArgumentResolver(
    private val memberQueryRepository: MemberQueryRepository,
    private val memberCommandRepository: MemberCommandRepository,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == Member::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Member {
        val authentication = SecurityContextHolder.getContext().authentication
        require(authentication.principal != "anonymousUser") { throw UnAuthorizedException(ErrorType.REQUIRE_LOGIN) }

        val oAuth2User = authentication.principal as OAuth2User
        val attributes = oAuth2User.attributes

        val name = attributes["name"].toString()
        val email = attributes["email"].toString()

        // NOTE: 구글 외 다른 로그인 수단 추가 시 아래 로직 변경 필요
        return memberQueryRepository.findByEmailOrNull(email)
            ?: memberCommandRepository.save(Member.createGoogleMember(name, email))
    }

    fun MemberQueryRepository.findByEmailOrNull(email: String): Member? = findByEmail(email).orElse(null)
}
