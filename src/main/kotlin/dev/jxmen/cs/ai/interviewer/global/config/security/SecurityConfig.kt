package dev.jxmen.cs.ai.interviewer.global.config.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.filter.OncePerRequestFilter

@Suppress("ktlint:standard:chain-method-continuation") // NOTE: 활성화시 오히려 가독성이 저하되어 비활성화
@EnableWebSecurity
@Configuration
class SecurityConfig(
    @Value("\${spring.profiles.active:default}") // NOTE: 값이 없을시 콜론 뒤에 기본값 지정 가능
    private val activeProfile: String,
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
            .sessionManagement {
                // 세션 유지하지 않도록 설정
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .csrf { it.disable() }
            .authorizeHttpRequests {
                // resources and public pages
                it
                    .requestMatchers("/h2-console/i**").permitAll()
                    .requestMatchers("/").permitAll()
                    .requestMatchers(HttpMethod.GET, "/error").permitAll()
                    .requestMatchers(HttpMethod.GET, "/favicon.ico").permitAll()
                    .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()

                // public API
                it
                    .requestMatchers(HttpMethod.GET, "/api/version").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/test/session-id").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/is-logged-in").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/subjects").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/subjects/{subjectId}/answer").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/chat/messages").permitAll()

                // v2 API
                it
                    .requestMatchers(HttpMethod.POST, "/api/v2/subjects/{subjectId}/answer").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v2/chat/messages").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/subjects/member").permitAll()
            }
            .oauth2Login { }
            .exceptionHandling {
                // 예외 발생시 커스텀 응답 반환
                it.authenticationEntryPoint(setCustomResponseAuthenticationEntryPoint())
            }
            .addFilterBefore(tokenFilter(), BasicAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun setCustomResponseAuthenticationEntryPoint(): AuthenticationEntryPoint = SetCustomResponseAuthenticationEntryPoint()

    @Bean
    fun tokenFilter(): OncePerRequestFilter = TokenFilter()
}
