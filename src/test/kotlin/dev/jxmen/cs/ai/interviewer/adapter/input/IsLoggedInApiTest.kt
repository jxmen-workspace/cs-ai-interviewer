package dev.jxmen.cs.ai.interviewer.adapter.input

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

@SpringBootTest
@TestPropertySource(properties = ["CLAUDE_API_KEY=test"])
class IsLoggedInApiTest {
    private lateinit var mockMvc: MockMvc

    private val restDocumentation = ManualRestDocumentation()

    @BeforeEach
    fun setUp() {
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(IsLoggedInApi())
                .apply<StandaloneMockMvcBuilder>(documentationConfiguration(restDocumentation))
                .build()

        restDocumentation.beforeTest(javaClass, javaClass.simpleName)
    }

    @AfterEach
    fun tearDown() {
        restDocumentation.afterTest()
    }

    @Test
    fun `isLoggedInApi는 반드시 false를 리턴한다`() {
        mockMvc
            .perform(
                RestDocumentationRequestBuilders
                    .get("/api/v1/is-logged-in"),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.isLoggedIn").value(false))
            .andDo(
                document(
                    identifier = "is-logged-in",
                    snippets =
                        arrayOf(
                            responseFields(
                                fieldWithPath("isLoggedIn").type(JsonFieldType.BOOLEAN).description("로그인 여부"),
                            ),
                        ),
                ),
            )
    }
}
