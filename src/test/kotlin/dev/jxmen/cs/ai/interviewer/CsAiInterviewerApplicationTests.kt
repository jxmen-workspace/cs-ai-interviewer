package dev.jxmen.cs.ai.interviewer

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = ["CLAUDE_API_KEY=test"])
class CsAiInterviewerApplicationTests {
    @Test
    fun contextLoads() {
    }
}
