package dev.jxmen.cs.ai.interviewer.persistence.entity.member

import dev.jxmen.cs.ai.interviewer.domain.member.MemberLoginType
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class MockMemberArgumentResolver : HandlerMethodArgumentResolver {
    companion object {
        val jpaMember =
            JpaMember.createWithId(
                id = 1L,
                name = "박주영",
                email = "me@jxmen.dev",
                loginType = MemberLoginType.GOOGLE,
            )
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.parameterType == JpaMember::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): JpaMember = jpaMember
}
