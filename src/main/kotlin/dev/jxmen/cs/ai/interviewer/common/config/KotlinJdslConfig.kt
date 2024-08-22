package dev.jxmen.cs.ai.interviewer.common.config

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KotlinJdslConfig {
    /**
     * 생성 비용이 크므로 미리 생성해 두는 것이 좋다.
     *
     * [Kotlin JDSL Documentation](https://kotlin-jdsl.gitbook.io/docs/jpql-with-kotlin-jdsl#execute-the-query)
     * [Related GitHub Issue](https://github.com/line/kotlin-jdsl/issues/727)
     */
    @Bean
    fun jpqlRenderContext(): JpqlRenderContext = JpqlRenderContext()
}
