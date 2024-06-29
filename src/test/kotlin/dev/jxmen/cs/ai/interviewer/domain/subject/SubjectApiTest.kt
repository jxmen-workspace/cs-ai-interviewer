package dev.jxmen.cs.ai.interviewer.domain.subject

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.fasterxml.jackson.databind.ObjectMapper
import dev.jxmen.cs.ai.interviewer.domain.subject.api.SubjectApi
import dev.jxmen.cs.ai.interviewer.domain.subject.dto.SubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.service.SubjectUseCase
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.util.LinkedMultiValueMap

class SubjectApiTest :
    DescribeSpec({
        val stubSubjectUseCase = StubSubjectUseCase()

        /**
         * without junit5 on spring rest docs, `ManualRestDocs` to generate api spec
         *
         * https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/#getting-started-documentation-snippets-setup-manual
         */
        val manualRestDocumentation = ManualRestDocumentation()

        lateinit var mockMvc: MockMvc

        beforeEach {
            mockMvc =
                MockMvcBuilders
                    .standaloneSetup(SubjectApi(stubSubjectUseCase))
                    .apply<StandaloneMockMvcBuilder>(documentationConfiguration(manualRestDocumentation))
                    .build()

            manualRestDocumentation.beforeTest(javaClass, javaClass.simpleName) // manual rest docs 사용시 필요
        }

        afterEach {
            manualRestDocumentation.afterTest() // manual rest docs 사용시 필요
        }

        describe("GET /api/subjects") {
            it("should return 200 with subject list") {
                val expectResponse =
                    ListDataResponse(
                        stubSubjectUseCase.getSubjectsByCategory("os").map {
                            SubjectResponse(
                                id = it.id,
                                title = it.title,
                                category = it.category,
                            )
                        },
                    )
                val queryParams = LinkedMultiValueMap<String, String>().apply { add("category", "os") }

                mockMvc
                    .perform(get("/api/subjects").queryParams(queryParams))
                    .andExpect(status().isOk)
                    .andExpect(content().json(toJson(expectResponse)))
                    .andDo(
                        document(
                            identifier = "get-subjects",
                            description = "주제 목록 조회",
                            snippets =
                                arrayOf(
                                    responseFields(
                                        fieldWithPath("data").description("데이터"),
                                        fieldWithPath("data[].id").description("주제 식별자"),
                                        fieldWithPath("data[].title").description("제목"),
                                        fieldWithPath("data[].category").description("카테고리"),
                                    ),
                                ),
                        ),
                    )
            }
        }
    }) {
    companion object {
        private val objectMapper = ObjectMapper()

        fun toJson(res: Any): String = objectMapper.writeValueAsString(res)
    }
}

class StubSubjectUseCase : SubjectUseCase {
    override fun getSubjectsByCategory(cateStr: String): List<Subject> =
        when (cateStr) {
            "dsa" -> listOf(Subject(title = "DSA", question = "What is DSA?", category = SubjectCategory.DSA))
            "network" -> listOf(Subject(title = "NETWORK", question = "What is Network?", category = SubjectCategory.NETWORK))
            "database" -> listOf(Subject(title = "DATABASE", question = "What is Database?", category = SubjectCategory.DATABASE))
            "os" -> listOf(Subject(title = "OS", question = "What is OS?", category = SubjectCategory.OS))
            else -> throw IllegalArgumentException("No such enum constant $cateStr")
        }
}
