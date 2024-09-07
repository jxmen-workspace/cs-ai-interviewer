package dev.jxmen.cs.ai.interviewer.persistence.entity.member

import dev.jxmen.cs.ai.interviewer.common.enum.ErrorType
import dev.jxmen.cs.ai.interviewer.common.exceptions.ServerError
import dev.jxmen.cs.ai.interviewer.common.exceptions.UnAuthorizedException
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberQueryRepository
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class JpaMemberArgumentResolver(
    private val memberQueryRepository: MemberQueryRepository,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == JpaMember::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): JpaMember {
        val authentication = SecurityContextHolder.getContext().authentication
        require(authentication != null) { throw UnAuthorizedException(ErrorType.REQUIRE_LOGIN) }
        require(authentication.principal != "anonymous") { throw UnAuthorizedException(ErrorType.REQUIRE_LOGIN) }
        require(authentication.principal != "anonymousUser") { throw UnAuthorizedException(ErrorType.REQUIRE_LOGIN) }

        val oAuth2User = authentication.principal as OAuth2User
        val attributes = oAuth2User.attributes

        // NOTE: 구글 외 다른 로그인 수단 추가 시 아래 로직 변경 필요
        val email = attributes["email"].toString()
        return memberQueryRepository.findByEmailOrNull(email) ?: throw ServerError(ErrorType.UNREGISTERED_MEMBER)
    }

    private fun MemberQueryRepository.findByEmailOrNull(email: String): JpaMember? = findByEmail(email).orElse(null)
}
