package dev.jxmen.cs.ai.interviewer.common.utils

import dev.jxmen.cs.ai.interviewer.common.enum.ErrorType
import dev.jxmen.cs.ai.interviewer.common.exceptions.ServerError
import dev.jxmen.cs.ai.interviewer.common.exceptions.UnAuthorizedException
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.mapper.MemberMapper
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberQueryRepository
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class MemberArgumentResolver(
    private val memberQueryRepository: MemberQueryRepository,
    private val memberMapper: MemberMapper,
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == Member::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Member {
        val authentication = SecurityContextHolder.getContext().authentication
        require(authentication != null) { throw UnAuthorizedException(ErrorType.REQUIRE_LOGIN) }
        require(authentication.principal != "anonymous") { throw UnAuthorizedException(ErrorType.REQUIRE_LOGIN) }
        require(authentication.principal != "anonymousUser") { throw UnAuthorizedException(ErrorType.REQUIRE_LOGIN) }

        val oAuth2User = authentication.principal as OAuth2User
        val attributes = oAuth2User.attributes

        // NOTE: 구글 외 다른 로그인 수단 추가 시 아래 로직 변경 필요
        val email = attributes["email"].toString()
        val jpaMember =
            memberQueryRepository.findByEmailOrNull(email) ?: throw ServerError(ErrorType.UNREGISTERED_MEMBER)

        return memberMapper.toDomain(jpaMember)
    }

    private fun MemberQueryRepository.findByEmailOrNull(email: String): JpaMember? = findByEmail(email).orElse(null)
}
