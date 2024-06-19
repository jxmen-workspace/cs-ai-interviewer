package dev.jxmen.cs.ai.interviewer.domain.subject

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.fasterxml.jackson.databind.ObjectMapper
import dev.jxmen.cs.ai.interviewer.domain.subject.api.SubjectApi
import dev.jxmen.cs.ai.interviewer.domain.subject.dto.SubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.service.SubjectService
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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

@WebMvcTest(SubjectApi::class)
class SubjectApiTest :
    DescribeSpec({
        val stubService = StubSubjectService(createDummySubjectRepository())

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
                    .standaloneSetup(SubjectApi(stubService))
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
                        stubService.getSubjectsByCategory("os").map {
                            SubjectResponse(
                                title = it.title,
                                question = it.question,
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
                                        fieldWithPath("data[].title").description("제목"),
                                        fieldWithPath("data[].question").description("질문"),
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

        fun createDummySubjectRepository(): SubjectRepository =
            object : SubjectRepository {
                override fun findByCategory(category: SubjectCategory): List<Subject> = emptyList()
            }
    }
}

class StubSubjectService(
    subjectRepository: SubjectRepository,
) : SubjectService(subjectRepository) {
    override fun getSubjectsByCategory(cateStr: String): List<Subject> =
        listOf(
            Subject(
                title = "os-title-1",
                question = "os-question-1",
                category = SubjectCategory.OS,
            ),
        )
}
