package dev.jxmen.cs.ai.interviewer.global.config

import dev.jxmen.cs.ai.interviewer.global.DatabaseInitializer
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class AppConfig(
    private val databaseInitializer: DatabaseInitializer,
) {
    @Bean
    @Profile("local", "default")
    fun databaseInitializerRunner() = ApplicationRunner { databaseInitializer.initData() }
}
