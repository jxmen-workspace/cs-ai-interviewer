package dev.jxmen.cs.ai.interviewer

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CsAiInterviewerApplicationTests(
    private val context: WebApplicationContext,
) : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    private lateinit var mockMvc: MockMvc

    init {
        beforeEach {
            mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        }

        describe("CsAiInterviewerApplicationTests") {
            it("context loads") {
                // contextLoads test logic
            }

            it("승인되지 않은 사용자 로그인 요청에 대해 401로 응답해야 합니다.") {
                listOf(
                    Pair(HttpMethod.GET, "/api/v1/subjects/1/chats"),
                    Pair(HttpMethod.GET, "/api/v1/subjects/my"),
                    Pair(HttpMethod.GET, "/api/v5/subjects/1/answer"),
                    Pair(HttpMethod.POST, "/api/v2/subjects/1/chats/archive"),
                ).forEach {
                    when (it.first) {
                        HttpMethod.GET -> mockMvc.get(it.second).andExpect { expectRequireLogin() }
                        HttpMethod.POST -> mockMvc.post(it.second).andExpect { expectRequireLogin() }
                        else -> throw IllegalArgumentException("Unsupported method: ${it.first}")
                    }
                }
            }

            val getMethodApis =
                listOf(
                    "/api/version",
                    "/api/v1/subjects",
                    "/api/v1/subjects/1",
                    "/api/v1/subjects/1/chats",
                    "/api/v1/subjects/my",
                    "/api/v5/subjects/1/answer",
                )
            getMethodApis.forEach { url ->
                it("지원되지 않는 메서드 호출에 대해 405로 응답해야 합니다. - $url") {
                    mockMvc
                        .post(url)
                        .andExpect {
                            status { isMethodNotAllowed() }
                        }
                }
            }
        }
    }

    private fun MockMvcResultMatchersDsl.expectRequireLogin() {
        status { isUnauthorized() }
        jsonPath("$.success") { value(false) }
        jsonPath("$.error.code") { value("REQUIRE_LOGIN") }
        jsonPath("$.error.status") { value(401) }
    }
}
