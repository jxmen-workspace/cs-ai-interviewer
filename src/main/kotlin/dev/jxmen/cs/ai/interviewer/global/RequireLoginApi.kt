package dev.jxmen.cs.ai.interviewer.global

import dev.jxmen.cs.ai.interviewer.global.config.security.JwtAuthenticationFilter

/**
 * 로그인이 필요한 API임을 명시하는 어노테이션
 *
 * @see JwtAuthenticationFilter
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequireLoginApi
