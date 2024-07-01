package dev.jxmen.cs.ai.interviewer.domain.subject

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.fasterxml.jackson.databind.ObjectMapper
import dev.jxmen.cs.ai.interviewer.domain.subject.api.SubjectApi
import dev.jxmen.cs.ai.interviewer.domain.subject.dto.response.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.dto.response.SubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectCategoryNotFoundException
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.domain.subject.service.port.SubjectQuery
import dev.jxmen.cs.ai.interviewer.global.GlobalControllerAdvice
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.JsonFieldType
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
        val stubSubjectQuery = StubSubjectQuery()

        /**
         * without junit5 on spring rest docs, `ManualRestDocs` to generate api spec
         *
         * https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/#getting-started-documentation-snippets-setup-manual
         */
        val manualRestDocumentation = ManualRestDocumentation()
        val controllerAdvice = GlobalControllerAdvice()

        lateinit var mockMvc: MockMvc

        beforeEach {
            mockMvc =
                MockMvcBuilders
                    .standaloneSetup(SubjectApi(stubSubjectQuery))
                    .setControllerAdvice(controllerAdvice)
                    .apply<StandaloneMockMvcBuilder>(documentationConfiguration(manualRestDocumentation))
                    .build()

            manualRestDocumentation.beforeTest(javaClass, javaClass.simpleName) // manual rest docs 사용시 필요
        }

        afterEach {
            manualRestDocumentation.afterTest() // manual rest docs 사용시 필요
        }

        describe("GET /api/subjects") {
            context("존재하는 카테고리 주제 목록 조회 요청 시") {
                it("200 상태코드와 주제 목록을 응답한다.") {
                    val expectResponse =
                        ListDataResponse(
                            stubSubjectQuery.getSubjectsByCategory("os").map {
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

            context("존재하지 않는 카테고리 주제 목록 조회 요청 시") {
                it("400를 응답한다.") {
                    val queryParams = LinkedMultiValueMap<String, String>().apply { add("category", "not_exist") }

                    mockMvc
                        .perform(get("/api/subjects").queryParams(queryParams))
                        .andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                identifier = "get-subjects-not-found",
                                description = "주제 목록 조회 실패",
                            ),
                        )
                }
            }

            context("카테고리 파라미터가 없는 요청일 경우") {
                it("400을 응답한다.") {
                    mockMvc
                        .perform(get("/api/subjects"))
                        .andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                identifier = "get-subjects-bad-request",
                                description = "주제 목록 조회 실패",
                            ),
                        )
                }
            }
        }

        describe("GET /api/subjects/{id}") {
            context("존재하는 주제 조회 시") {
                it("should return 200 with subject") {
                    val subject = stubSubjectQuery.getSubjectById(1L)
                    val expectResponse =
                        SubjectDetailResponse(
                            id = subject.id,
                            category = subject.category,
                            title = subject.title,
                            question = subject.question,
                        )

                    mockMvc
                        .perform(get("/api/subjects/1"))
                        .andExpect(status().isOk)
                        .andExpect(content().json(toJson(expectResponse)))
                        .andDo(
                            document(
                                identifier = "get-subject-success",
                                description = "주제 상세 조회",
                                snippets =
                                    arrayOf(
                                        responseFields(
                                            fieldWithPath("id").description("주제 식별자").type(JsonFieldType.NUMBER),
                                            fieldWithPath("title").description("제목").type(JsonFieldType.STRING),
                                            fieldWithPath("category").description("카테고리").type(JsonFieldType.STRING),
                                            fieldWithPath("question").description("질문").type(JsonFieldType.STRING),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("존재하지 않는 주제 조회 시") {
                it("404를 응답한다.") {
                    mockMvc
                        .perform(get("/api/subjects/${StubSubjectQuery.NOT_FOUND_ID}"))
                        .andExpect(status().isNotFound)
                        .andDo(
                            document(
                                identifier = "get-subject-not-found",
                                description = "주제 상세 조회 실패",
                            ),
                        )
                }
            }
        }
    }) {
    companion object {
        private val objectMapper = ObjectMapper()

        fun toJson(res: Any): String = objectMapper.writeValueAsString(res)
    }
}

class StubSubjectQuery : SubjectQuery {
    companion object {
        val NOT_FOUND_ID = 10000L
    }

    override fun getSubjectsByCategory(cateStr: String): List<Subject> =
        when (cateStr) {
            "dsa" -> listOf(Subject(title = "DSA", question = "What is DSA?", category = SubjectCategory.DSA))
            "network" -> listOf(Subject(title = "NETWORK", question = "What is Network?", category = SubjectCategory.NETWORK))
            "database" -> listOf(Subject(title = "DATABASE", question = "What is Database?", category = SubjectCategory.DATABASE))
            "os" -> listOf(Subject(title = "OS", question = "What is OS?", category = SubjectCategory.OS))
            else -> throw SubjectCategoryNotFoundException("No such enum constant $cateStr")
        }

    override fun getSubjectById(id: Long): Subject {
        if (id == NOT_FOUND_ID) throw SubjectNotFoundException(100L)

        return Subject(title = "OS", question = "What is OS?", category = SubjectCategory.OS)
    }
}
