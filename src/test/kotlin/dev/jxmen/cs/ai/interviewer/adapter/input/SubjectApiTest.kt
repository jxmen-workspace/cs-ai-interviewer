package dev.jxmen.cs.ai.interviewer.adapter.input

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.fasterxml.jackson.databind.ObjectMapper
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.SubjectAnswerRequest
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectAnswerResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.response.SubjectResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommandV2
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.Chats
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MockMemberArgumentResolver
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectCategoryNotFoundException
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundExceptionV2
import dev.jxmen.cs.ai.interviewer.global.GlobalControllerAdvice
import dev.jxmen.cs.ai.interviewer.global.dto.ListDataResponse
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.util.LinkedMultiValueMap

class SubjectApiTest :
    DescribeSpec({

        lateinit var subjectQuery: SubjectQuery
        lateinit var chatQuery: ChatQuery

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
                    .standaloneSetup(SubjectApi(subjectQuery, StubSubjectUseCase(), chatQuery))
                    .setControllerAdvice(controllerAdvice)
                    .setCustomArgumentResolvers(MockMemberArgumentResolver())
                    .apply<StandaloneMockMvcBuilder>(documentationConfiguration(manualRestDocumentation))
                    .build()

            manualRestDocumentation.beforeTest(javaClass, javaClass.simpleName) // manual rest docs 사용시 필요
        }

        afterEach {
            manualRestDocumentation.afterTest() // manual rest docs 사용시 필요
        }

        describe("GET /api/subjects") {
            chatQuery = DummyChatQuery()

            context("존재하는 카테고리 주제 목록 조회 요청 시") {
                subjectQuery = ExistCategorySubjectQueryStub()
                val category = "os"

                it("200 상태코드와 주제 목록을 응답한다.") {
                    val expectResponse =
                        ListDataResponse(
                            subjectQuery.findByCategory(category).map {
                                SubjectResponse(
                                    id = it.id,
                                    title = it.title,
                                    category = it.category,
                                )
                            },
                        )
                    val queryParams = LinkedMultiValueMap<String, String>().apply { add("category", category) }

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
                subjectQuery = NotExistCategorySubjectQuery()

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
                subjectQuery = DummySubjectQuery()

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
            chatQuery = DummyChatQuery()

            context("존재하는 주제 조회 시") {
                val id = 1
                subjectQuery = ExistIdSubjectQuery()
                it("should return 200 with subject") {
                    val expectResponse =
                        SubjectDetailResponse(
                            id = id.toLong(),
                            title = "test subject",
                            question = "test question",
                            category = SubjectCategory.OS,
                        )

                    mockMvc
                        .perform(get("/api/subjects/$id"))
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
                subjectQuery = NotExistIdSubjectQuery()
                val id = 99999

                it("404를 응답한다.") {
                    mockMvc
                        .perform(get("/api/subjects/$id"))
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

        describe("POST /api/v2/subjects/{id}/answer 요청은") {

            context("존재하는 주제에 대한 답변 요청 시") {
                val id = 1
                subjectQuery = ExistIdSubjectQuery()
                chatQuery = ExistSubjectIdChatQuery()

                it("201 상태코드와 재질문이 포함된 응답을 반환한다.") {
                    val req = SubjectAnswerRequest(answer = "answer")
                    val expectResponse =
                        SubjectAnswerResponse(nextQuestion = "What is OS? (answer: answer)", score = 50)

                    mockMvc
                        .perform(
                            post("/api/v2/subjects/$id/answer")
                                .header("Authorization", "Bearer token")
                                .content(toJson(req))
                                .contentType(MediaType.APPLICATION_JSON),
                        ).andExpect(status().isCreated)
                        .andExpect(content().json(toJson(expectResponse)))
                        .andDo(
                            document(
                                identifier = "post-subject-answer",
                                description = "주제 답변 요청",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer token"),
                                        ),
                                        responseFields(
                                            fieldWithPath("nextQuestion").description("다음 질문").type(JsonFieldType.STRING),
                                            fieldWithPath("score").description("답변에 대한 점수").type(JsonFieldType.NUMBER),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("답변을 모두 사용했을 경우") {
                val id = 2
                subjectQuery = ExistIdSubjectQuery()
                chatQuery = UseAllAnswersChatQuery()

                it("400을 응답한다.") {
                    val req = SubjectAnswerRequest(answer = "answer")

                    mockMvc
                        .perform(
                            post("/api/v2/subjects/$id/answer")
                                .header("Authorization", "Bearer token")
                                .content(toJson(req))
                                .contentType(MediaType.APPLICATION_JSON),
                        ).andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                identifier = "post-subject-answer-bad-request",
                                description = "답변을 모두 사용했을 경우",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer token"),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("존재하지 않는 주제에 대한 답변 요청 시") {
                val id = 99999
                subjectQuery = NotExistIdSubjectQuery()
                chatQuery = DummyChatQuery()

                it("404를 응답한다.") {
                    val req = SubjectAnswerRequest(answer = "answer")

                    val perform =
                        mockMvc
                            .perform(
                                post("/api/v2/subjects/$id/answer")
                                    .header("Authorization", "Bearer token")
                                    .content(toJson(req))
                                    .contentType(MediaType.APPLICATION_JSON),
                            )

                    perform
                        .andExpect(status().isNotFound)
                        .andDo(
                            document(
                                identifier = "post-subject-answer-not-found",
                                description = "존재하지 않는 답변 요청",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer token"),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("답변이 없는 요청 시") {
                val id = 3
                subjectQuery = DummySubjectQuery()
                chatQuery = DummyChatQuery()

                it("400를 응답한다.") {
                    val req = SubjectAnswerRequest(answer = "")

                    val perform =
                        mockMvc
                            .perform(
                                post("/api/v2/subjects/$id/answer")
                                    .header("Authorization", "Bearer token")
                                    .content(toJson(req))
                                    .contentType(MediaType.APPLICATION_JSON),
                            )

                    perform
                        .andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                identifier = "post-subject-answer-bad-request",
                                description = "답변이 없는 요청",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer token"),
                                        ),
                                    ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/subjects/member 요청은") {
            chatQuery = DummyChatQuery()

            context("로그인한 사용자가 카테고리 없이 요청 시") {
                subjectQuery = NoCategoryMemberSubjectQuery()

                it("200 상태코드와 전체 주제 목록을 응답한다.") {
                    mockMvc
                        .perform(
                            get("/api/v1/subjects/member")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.data").isArray)
                        .andExpect(jsonPath("$.data.length()").value(2))
                        .andExpect(jsonPath("$.data[0].id").value(1))
                        .andExpect(jsonPath("$.data[0].title").value("title1"))
                        .andExpect(jsonPath("$.data[0].category").value("OS"))
                        .andExpect(jsonPath("$.data[0].maxScore").value(100))
                        .andExpect(jsonPath("$.data[1].id").value(2))
                        .andExpect(jsonPath("$.data[1].title").value("title2"))
                        .andExpect(jsonPath("$.data[1].category").value("NETWORK"))
                        .andExpect(jsonPath("$.data[1].maxScore").value(70))
                        .andDo(
                            document(
                                identifier = "get-subjects-member",
                                description = "로그인한 사용자의 주제 목록 조회",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer 토큰"),
                                        ),
                                        responseFields(
                                            fieldWithPath("data").description("데이터"),
                                            fieldWithPath("data[].id").description("주제 식별자"),
                                            fieldWithPath("data[].title").description("제목"),
                                            fieldWithPath("data[].category").description("카테고리"),
                                            fieldWithPath("data[].maxScore").description("최대 점수"),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("로그인한 사용자가 카테고리와 함께 요청 시") {
                subjectQuery = WithCategoryMemberSubjectQuery()

                it("200 상태코드와 해당 카테고리 주제 목록을 응답한다.") {
                    mockMvc
                        .get("/api/v1/subjects/member") { param("category", "os") }
                        .andExpect {
                            status { isOk() }
                            jsonPath("$.data") { isArray() }
                            jsonPath("$.data.length()") { value(1) }
                            jsonPath("$.data[0].id") { value(1) }
                            jsonPath("$.data[0].title") { value("title1") }
                            jsonPath("$.data[0].category") { value("OS") }
                            jsonPath("$.data[0].maxScore") { value(100) }
                        }
                }
            }
        }
    }) {
    companion object {
        private val objectMapper = ObjectMapper()

        fun toJson(res: Any): String = objectMapper.writeValueAsString(res)
    }

    open abstract class StubSubjectQuery : SubjectQuery {
        override fun findByCategory(cateStr: String): List<Subject> = throw NotImplementedError("Not implemented")

        override fun findWithMember(
            member: Member,
            category: String?,
        ): List<MemberSubjectResponse> = throw NotImplementedError("Not implemented")

        override fun findByIdOrThrow(id: Long): Subject = throw NotImplementedError("Not implemented")

        override fun findByIdOrThrowV2(id: Long): Subject = throw NotImplementedError("Not implemented")
    }

    class ExistIdSubjectQuery : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): Subject = Subject.createOS(id = id, title = "test subject", question = "test question")

        override fun findByIdOrThrowV2(id: Long): Subject = findByIdOrThrow(id)
    }

    class StubSubjectUseCase : SubjectUseCase {
        override fun answerV2(command: CreateSubjectAnswerCommandV2): SubjectAnswerResponse {
            val chats = Chats(command.chats)
            require(!chats.useAllAnswers()) { throw AllAnswersUsedException("답변을 모두 사용했습니다.") }

            return SubjectAnswerResponse(nextQuestion = "What is OS? (answer: ${command.answer})", score = 50)
        }
    }

    class ExistCategorySubjectQueryStub : StubSubjectQuery() {
        override fun findByCategory(cateStr: String): List<Subject> =
            when (cateStr) {
                "os" -> listOf(Subject.createOS(id = 1, title = "OS", question = "What is OS?"))
                else -> throw SubjectCategoryNotFoundException("No such enum constant $cateStr")
            }
    }

    class DummyChatQuery : ChatQuery {
        override fun findBySubjectAndMember(
            subject: Subject,
            member: Member,
        ): List<Chat> = throw NotImplementedError("Not implemented")
    }

    class NotExistCategorySubjectQuery : StubSubjectQuery() {
        override fun findByCategory(cateStr: String) = throw SubjectCategoryNotFoundException(cateStr)
    }

    class NotExistIdSubjectQuery : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): Subject = throw SubjectNotFoundException(id)

        override fun findByIdOrThrowV2(id: Long): Subject = throw SubjectNotFoundExceptionV2(id)
    }

    class ExistSubjectIdChatQuery : ChatQuery {
        override fun findBySubjectAndMember(
            subject: Subject,
            member: Member,
        ): List<Chat> =
            listOf(
                Chat.createQuestion(
                    subject = subject,
                    member = member,
                    nextQuestion = "What is OS?",
                ),
            )
    }

    class UseAllAnswersChatQuery : ChatQuery {
        override fun findBySubjectAndMember(
            subject: Subject,
            member: Member,
        ): List<Chat> {
            val chats = mutableListOf<Chat>()
            for (i in 1..Chat.MAX_ANSWER_COUNT * 2) {
                if (i % 2 == 0) {
                    chats.add(Chat.createAnswer(subject = subject, member = member, answer = "hi", score = 0))
                } else {
                    chats.add(Chat.createQuestion(subject = subject, member = member, nextQuestion = "What is OS?"))
                }
            }

            return chats
        }
    }

    class DummySubjectQuery : SubjectQuery {
        override fun findByCategory(cateStr: String): List<Subject> = throw NotImplementedError()

        override fun findByIdOrThrow(id: Long): Subject = throw NotImplementedError()

        override fun findWithMember(
            member: Member,
            category: String?,
        ): List<MemberSubjectResponse> = throw NotImplementedError()

        override fun findByIdOrThrowV2(id: Long): Subject = throw NotImplementedError()
    }

    class NoCategoryMemberSubjectQuery : StubSubjectQuery() {
        override fun findWithMember(
            member: Member,
            category: String?,
        ): List<MemberSubjectResponse> =
            listOf(
                MemberSubjectResponse(
                    id = 1,
                    title = "title1",
                    category = SubjectCategory.OS,
                    maxScore = 100,
                ),
                MemberSubjectResponse(
                    id = 2,
                    title = "title2",
                    category = SubjectCategory.NETWORK,
                    maxScore = 70,
                ),
            )
    }

    class WithCategoryMemberSubjectQuery : StubSubjectQuery() {
        override fun findWithMember(
            member: Member,
            category: String?,
        ): List<MemberSubjectResponse> =
            listOf(
                MemberSubjectResponse(
                    id = 1,
                    title = "title1",
                    category = SubjectCategory.OS,
                    maxScore = 100,
                ),
            )
    }
}
