package dev.jxmen.cs.ai.interviewer.global.config

import dev.jxmen.cs.ai.interviewer.domain.member.MemberArgumentResolver
import dev.jxmen.cs.ai.interviewer.domain.member.MemberQueryRepository
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val memberQueryRepository: MemberQueryRepository,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000",
                "https://cs-ai-interviewer-web.vercel.app",
                "https://cs-ai.jxmen.dev",
            ).allowCredentials(true)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
            .allowedHeaders("*")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(
            MemberArgumentResolver(memberQueryRepository = memberQueryRepository),
        )
    }
}
