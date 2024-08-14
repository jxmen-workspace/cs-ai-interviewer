package dev.jxmen.cs.ai.interviewer.global.config

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KotlinJdslConfig {
    @Bean
    fun jpqlRenderContext(): JpqlRenderContext = JpqlRenderContext()
}
