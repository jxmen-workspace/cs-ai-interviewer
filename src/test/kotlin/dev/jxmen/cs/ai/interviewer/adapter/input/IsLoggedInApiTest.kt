package dev.jxmen.cs.ai.interviewer.adapter.input

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

@WebMvcTest(IsLoggedInApi::class)
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
    fun `로그인한 유저는 isLoggedIn True를 리턴한다`() {
        val mockHttpSession = MockHttpSession()
        mockHttpSession.setAttribute("member", mock())

        mockMvc
            .perform(
                RestDocumentationRequestBuilders
                    .get("/api/v1/is-logged-in")
                    .session(mockHttpSession),
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.isLoggedIn").value(true))
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

    @Test
    fun `로그인하지 않은 유저는 isLoggedIn False를 리턴한다`() {
        mockMvc
            .get("/api/v1/is-logged-in")
            .andExpect {
                status { isOk() }
                jsonPath("$.isLoggedIn") { value(false) }
            }
    }
}
