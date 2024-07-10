package dev.jxmen.cs.ai.interviewer

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@TestPropertySource(properties = ["CLAUDE_API_KEY=test"])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class VersionApiTest(
    private val context: WebApplicationContext,
) : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    val manualRestDocumentation = ManualRestDocumentation()
    private lateinit var mockMvc: MockMvc

    init {
        beforeEach {
            mockMvc =
                MockMvcBuilders
                    .webAppContextSetup(context)
                    .apply<DefaultMockMvcBuilder>(documentationConfiguration(manualRestDocumentation))
                    .build()
            manualRestDocumentation.beforeTest(javaClass, javaClass.simpleName)
        }

        afterEach {
            manualRestDocumentation.afterTest()
        }

        describe("GET /api/version 요청은") {
            it("현재 프로젝트 버전을 응답한다") {
                mockMvc
                    .perform(get("/api/version"))
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            identifier = "get-version",
                            description = "배포된 버전 조회",
                            snippets = (
                                arrayOf(
                                    responseFields(
                                        fieldWithPath("version").description("현재 프로젝트 버전").type(JsonFieldType.STRING),
                                    ),
                                )
                            ),
                        ),
                    )
            }
        }
    }
}
