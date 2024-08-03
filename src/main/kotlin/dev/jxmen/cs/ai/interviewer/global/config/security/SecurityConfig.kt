package dev.jxmen.cs.ai.interviewer.global.config.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.jxmen.cs.ai.interviewer.global.dto.ErrorResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.csrf.CsrfTokenRequestHandler
import org.springframework.web.filter.OncePerRequestFilter

@Suppress("ktlint:standard:chain-method-continuation") // NOTE: 활성화시 오히려 가독성이 저하되어 비활성화
@EnableWebSecurity
@Configuration
class SecurityConfig(
    @Value("\${spring.profiles.active:default}") // NOTE: 값이 없을시 콜론 뒤에 기본값 지정 가능
    private val activeProfile: String,
) {
    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        if (activeProfile == "local" || activeProfile == "default") {
            // set h2 console to be accessible
            http.headers {
                it.frameOptions { it.disable() }
            }
        }

        http
            .csrf {
                it.ignoringRequestMatchers("/h2-console/**")
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                it.csrfTokenRequestHandler(spaCsrfTokenRequestHandler())
            }
            .authorizeHttpRequests {
                // resources and public pages
                it
                    .requestMatchers("/h2-console/**").permitAll()
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
            }
            .oauth2Login { }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, authException ->
                    // 인증되지 않거나 실패할 경우 공개된 API 외 401 응답
                    response.status = HttpStatus.UNAUTHORIZED.value()
                    response.setHeader("Content-Type", MediaType.APPLICATION_JSON.type)
                    response.writer.write(
                        toJson(
                            ErrorResponse(
                                message = authException.message ?: "Unauthorized",
                                status = HttpStatus.UNAUTHORIZED.value(),
                            ),
                        ),
                    )
                }
            }
            .addFilterBefore(googleTokenFilter(), BasicAuthenticationFilter::class.java)
            .addFilterAfter(csrfCookieFilter(), BasicAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun googleTokenFilter(): OncePerRequestFilter = TokenFilter()

    @Bean
    fun spaCsrfTokenRequestHandler(): CsrfTokenRequestHandler = SpaCsrfTokenRequestHandler()

    @Bean
    fun csrfCookieFilter(): CsrfCookieFilter = CsrfCookieFilter()

    private fun toJson(obj: Any): String = objectMapper.writeValueAsString(obj)
}
