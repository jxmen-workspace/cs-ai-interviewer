package dev.jxmen.cs.ai.interviewer.common.config.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Suppress("ktlint:standard:chain-method-continuation") // NOTE: 활성화시 오히려 가독성이 저하되어 비활성화
@EnableWebSecurity
@Configuration
class SecurityConfig(
    @Value("\${spring.profiles.active:default}") // NOTE: 값이 없을시 콜론 뒤에 기본값 지정 가능
    private val activeProfile: String,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        if (activeProfile == "local" || activeProfile == "default") {
            // set h2 console to be accessible
            http
                .headers {
                    it.frameOptions { it.disable() }
                }
        }

        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // NOTE: authorizeHttpRequests는 controller에서 설정
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(refererCaptureFilter(), OAuth2AuthorizationRequestRedirectFilter::class.java)
            .oauth2Login {
                it.successHandler(oAuth2LoginSuccessHandler)
            }
            .exceptionHandling {
                // 예외 발생시 커스텀 응답 반환
                it.authenticationEntryPoint(setCustomResponseAuthenticationEntryPoint())
            }

        return http.build()
    }

    @Bean
    fun setCustomResponseAuthenticationEntryPoint(): AuthenticationEntryPoint = SetCustomResponseAuthenticationEntryPoint()

    @Bean
    fun refererCaptureFilter(): OncePerRequestFilter = RefererCaptureFilter()
}
