package dev.jxmen.cs.ai.interviewer.global.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.jxmen.cs.ai.interviewer.global.config.service.CustomOAuth2UserService
import dev.jxmen.cs.ai.interviewer.global.dto.ErrorResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Suppress("ktlint:standard:chain-method-continuation") // NOTE: 활성화시 오히려 가독성이 저하되어 비활성화
@EnableWebSecurity
@Configuration
class SecurityConfig(
    @Value("\${spring.profiles.active:default}") // NOTE: 값이 없을시 콜론 뒤에 기본값 지정 가능
    private val activeProfile: String,
    private val customOAuth2UserService: CustomOAuth2UserService,
) {
    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        if (activeProfile == "local" || activeProfile == "default") {
            // set h2 console to be accessible
            http
                .csrf { it.disable() }
                .headers {
                    it.frameOptions { it.disable() }
                }
        }

        http
            .authorizeHttpRequests {
                it.requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/api/version", "/api/test/**").permitAll()
                    .requestMatchers("/api/subjects", "/api/subjects/**").permitAll()
                    .anyRequest().authenticated()
            }.oauth2Login {
                it.userInfoEndpoint {
                    it.userService(customOAuth2UserService)
                }
            }.exceptionHandling {
                it.authenticationEntryPoint { request, response, authException ->
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

        return http.build()
    }

    private fun toJson(obj: Any): String = objectMapper.writeValueAsString(obj)
}
