package dev.jxmen.cs.ai.interviewer.presentation

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.WebTestClientRestDocumentationWrapper
import com.fasterxml.jackson.databind.ObjectMapper
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatAnswerUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatArchiveUseCase
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.dto.CreateSubjectAnswerCommand
import dev.jxmen.cs.ai.interviewer.common.GlobalControllerAdvice
import dev.jxmen.cs.ai.interviewer.common.dto.ApiResponse
import dev.jxmen.cs.ai.interviewer.common.dto.ListDataResponse
import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.NoAnswerException
import dev.jxmen.cs.ai.interviewer.domain.member.MockMemberArgumentResolver
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectCategoryNotFoundException
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.MockJpaMemberArgumentResolver
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.presentation.dto.response.SubjectDetailResponse
import dev.jxmen.cs.ai.interviewer.presentation.dto.response.SubjectResponse
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import org.springframework.util.LinkedMultiValueMap
import reactor.core.publisher.Flux
import java.time.LocalDateTime

class SubjectApiTest :
    DescribeSpec({

        /**
         * without junit5 on spring rest docs, `ManualRestDocs` to generate api spec
         *
         * https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/#getting-started-documentation-snippets-setup-manual
         */
        val manualRestDocumentation = ManualRestDocumentation()
        val controllerAdvice = GlobalControllerAdvice()

        var subjectQuery: SubjectQuery = DummySubjectQuery()
        var chatQuery: ChatQuery = DummyChatQuery()
        var chatAnswerUseCase: ChatAnswerUseCase = StubChatAnswerUseCase()

        lateinit var mockMvc: MockMvc
        lateinit var webTestClient: WebTestClient

        beforeEach {
            mockMvc =
                MockMvcBuilders
                    .standaloneSetup(SubjectApi(subjectQuery, chatQuery, chatAnswerUseCase, StubChatArchiveUseCase()))
                    .setControllerAdvice(controllerAdvice)
                    .setCustomArgumentResolvers(
                        MockJpaMemberArgumentResolver(),
                        MockMemberArgumentResolver(),
                    ).apply<StandaloneMockMvcBuilder>(documentationConfiguration(manualRestDocumentation))
                    .build()
            webTestClient =
                MockMvcWebTestClient
                    .bindToController(SubjectApi(subjectQuery, chatQuery, chatAnswerUseCase, StubChatArchiveUseCase()))
                    .controllerAdvice(controllerAdvice)
                    .customArgumentResolvers(
                        MockJpaMemberArgumentResolver(),
                        MockMemberArgumentResolver(),
                    ).configureClient()
                    .filter(
                        WebTestClientRestDocumentation.documentationConfiguration(manualRestDocumentation),
                    ).build()

            manualRestDocumentation.beforeTest(javaClass, javaClass.simpleName) // manual rest docs 사용시 필요
        }

        afterEach {
            manualRestDocumentation.afterTest() // manual rest docs 사용시 필요
        }

        describe("GET /api/v1/subjects") {
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
                        .perform(get("/api/v1/subjects").queryParams(queryParams))
                        .andExpect(status().isOk)
                        .andExpect(content().json(toJson(expectResponse)))
                        .andDo(
                            document(
                                identifier = "get-subjects",
                                description = "주제 목록 조회",
                                snippets =
                                    arrayOf(
                                        responseFields(
                                            fieldWithPath("success").description("성공 여부").type(JsonFieldType.BOOLEAN),
                                            fieldWithPath("data[].id").description("주제 식별자").type(JsonFieldType.NUMBER),
                                            fieldWithPath("data[].title").description("제목").type(JsonFieldType.STRING),
                                            fieldWithPath("data[].category").description("카테고리").type(JsonFieldType.STRING),
                                            fieldWithPath("error")
                                                .description("에러 정보")
                                                .type(JsonFieldType.OBJECT)
                                                .optional(),
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
                        .perform(get("/api/v1/subjects").queryParams(queryParams))
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
                        .perform(get("/api/v1/subjects"))
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

        describe("GET /api/v1/subjects/{id}") {
            context("존재하는 주제 조회 시") {
                val id = 1
                subjectQuery = ExistIdSubjectQuery()
                it("should return 200 with subject") {
                    val expectResponse =
                        ApiResponse.success(
                            SubjectDetailResponse(
                                id = id.toLong(),
                                title = "test subject",
                                question = "test question",
                                category = SubjectCategory.OS,
                            ),
                        )

                    mockMvc
                        .perform(get("/api/v1/subjects/$id"))
                        .andExpect(status().isOk)
                        .andExpect(content().json(toJson(expectResponse)))
                        .andDo(
                            document(
                                identifier = "get-subject-success",
                                description = "주제 상세 조회",
                                snippets =
                                    arrayOf(
                                        responseFields(
                                            fieldWithPath("success").description("성공 여부").type(JsonFieldType.BOOLEAN),
                                            fieldWithPath("data.id").description("주제 식별자").type(JsonFieldType.NUMBER),
                                            fieldWithPath("data.title").description("제목").type(JsonFieldType.STRING),
                                            fieldWithPath("data.category").description("카테고리").type(JsonFieldType.STRING),
                                            fieldWithPath("data.question").description("질문").type(JsonFieldType.STRING),
                                            fieldWithPath("error")
                                                .description("에러 정보")
                                                .type(JsonFieldType.OBJECT)
                                                .optional(),
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
                        .perform(get("/api/v1/subjects/$id"))
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

        describe("GET /api/v5/subjects/{id}/answer 요청은") {

            context("존재하는 주제에 대한 답변 요청 시") {
                val id = 1

                chatAnswerUseCase =
                    object : ChatAnswerUseCase {
                        override fun answer(command: CreateSubjectAnswerCommand): Flux<ChatResponse> =
                            Flux.just(
                                ChatResponse(
                                    listOf(
                                        Generation("답변 내용"),
                                    ),
                                ),
                            )
                    }

                it("200을 응답한다") {
                    webTestClient
                        .get()
                        .uri("/api/v5/subjects/$id/answer?message={message}", "answer")
                        .header("Authorization", "Bearer token")
                        .exchange()
                        .expectStatus()
                        .isOk
                        .expectBody()
                        .consumeWith(
                            WebTestClientRestDocumentationWrapper
                                .document(
                                    identifier = "get-subject-answer",
                                    description = "주제 답변 요청",
                                    snippets =
                                        arrayOf(
                                            requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰"),
                                            ),
                                            responseFields(
                                                fieldWithPath(
                                                    "[].result.metadata.contentFilterMetadata",
                                                ).type(JsonFieldType.NULL).description("Content filter metadata"),
                                                fieldWithPath(
                                                    "[].result.metadata.finishReason",
                                                ).type(JsonFieldType.NULL).description("Finish reason"),
                                                fieldWithPath(
                                                    "[].result.output.messageType",
                                                ).type(JsonFieldType.STRING).description("Message type"),
                                                fieldWithPath("[].result.output.media").type(JsonFieldType.ARRAY).description("Media"),
                                                fieldWithPath(
                                                    "[].result.output.metadata.messageType",
                                                ).type(JsonFieldType.STRING).description("Message type in metadata"),
                                                fieldWithPath("[].result.output.content").type(JsonFieldType.STRING).description("답변 내용"),
                                                fieldWithPath("[].metadata").type(JsonFieldType.OBJECT).description("Metadata"),
                                                fieldWithPath(
                                                    "[].results[].metadata.contentFilterMetadata",
                                                ).type(JsonFieldType.NULL).description("Content filter metadata"),
                                                fieldWithPath(
                                                    "[].results[].metadata.finishReason",
                                                ).type(JsonFieldType.NULL).description("Finish reason"),
                                                fieldWithPath(
                                                    "[].results[].output.messageType",
                                                ).type(JsonFieldType.STRING).description("Message type"),
                                                fieldWithPath("[].results[].output.media").type(JsonFieldType.ARRAY).description("Media"),
                                                fieldWithPath(
                                                    "[].results[].output.metadata.messageType",
                                                ).type(JsonFieldType.STRING).description("Message type in metadata"),
                                                fieldWithPath(
                                                    "[].results[].output.content",
                                                ).type(JsonFieldType.STRING).description("답변 내용"),
                                            ),
                                        ),
                                ),
                        )
                }
            }

            context("답변을 모두 사용했을 경우") {
                val id = 2

                chatAnswerUseCase =
                    object : ChatAnswerUseCase {
                        override fun answer(command: CreateSubjectAnswerCommand): Flux<ChatResponse> = Flux.error(NoAnswerException())
                    }

                it("400을 응답한다") {
                    webTestClient
                        .get()
                        .uri("/api/v5/subjects/$id/answer?message={message}", "answer")
                        .header("Authorization", "Bearer token")
                        .exchange()
                        .expectStatus()
                        .isBadRequest
                        .expectBody()
                        .consumeWith(
                            WebTestClientRestDocumentationWrapper
                                .document(
                                    identifier = "get-subject-answer-bad-request",
                                    description = "답변을 모두 사용했을 경우",
                                    snippets =
                                        arrayOf(
                                            requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰"),
                                            ),
                                        ),
                                ),
                        )
                }
            }

            context("존재하지 않는 주제에 대한 답변 요청 시") {
                val id = 99999

                chatAnswerUseCase =
                    object : ChatAnswerUseCase {
                        override fun answer(command: CreateSubjectAnswerCommand): Flux<ChatResponse> =
                            Flux.error(SubjectNotFoundException(id.toLong()))
                    }

                it("404를 응답한다") {
                    webTestClient
                        .get()
                        .uri("/api/v5/subjects/$id/answer?message={message}", "answer")
                        .header("Authorization", "Bearer token")
                        .exchange()
                        .expectStatus()
                        .isNotFound
                        .expectBody()
                        .consumeWith(
                            WebTestClientRestDocumentationWrapper
                                .document(
                                    identifier = "get-subject-answer-not-found",
                                    description = "존재하지 않는 답변 요청",
                                    snippets =
                                        arrayOf(
                                            requestHeaders(
                                                headerWithName("Authorization").description("Bearer 토큰"),
                                            ),
                                        ),
                                ),
                        )
                }
            }
        }

        describe("GET /api/v1/subjects/my 요청은") {

            context("로그인한 사용자가 카테고리 없이 요청 시") {
                subjectQuery = NoCategoryMemberSubjectQuery()

                it("200 상태코드와 전체 주제 목록을 응답한다.") {
                    mockMvc
                        .perform(
                            get("/api/v1/subjects/my")
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
                                identifier = "get-subjects-my",
                                description = "로그인한 사용자의 주제 목록 조회",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer 토큰"),
                                        ),
                                        responseFields(
                                            fieldWithPath("success").description("성공 여부").type(JsonFieldType.BOOLEAN),
                                            fieldWithPath("data[].id").description("주제 식별자").type(JsonFieldType.NUMBER),
                                            fieldWithPath("data[].title").description("제목").type(JsonFieldType.STRING),
                                            fieldWithPath("data[].category").description("카테고리").type(JsonFieldType.STRING),
                                            fieldWithPath("data[].maxScore")
                                                .description("최대 점수")
                                                .type(JsonFieldType.NUMBER),
                                            fieldWithPath("error").description("에러").type(JsonFieldType.OBJECT).optional(),
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
                        .get("/api/v1/subjects/my") {
                            param("category", "os")
                            header("Cookie", "SESSION=sessionId")
                        }.andExpect {
                            status { isOk() }
                            jsonPath("$.success") { value(true) }
                            jsonPath("$.data.length()") { value(1) }
                            jsonPath("$.data[0].id") { value(1) }
                            jsonPath("$.data[0].title") { value("title1") }
                            jsonPath("$.data[0].category") { value("OS") }
                            jsonPath("$.data[0].maxScore") { value(100) }
                            jsonPath("$.error") { isEmpty() }
                        }
                }
            }
        }

        describe("GET /api/v1/subjects/{subjectId}/chats 요청은") {

            context("subjectId가 존재할경우") {
                val id = 1
                val date = LocalDateTime.of(2024, 8, 15, 21, 0, 0)
                subjectQuery = ExistIdSubjectQuery()
                chatQuery = ExistingSubjectIdChatQuery(date)

                it("200 OK와 Chat 객체를 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v1/subjects/$id/chats")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.length()").value(3))
                        .andExpect(jsonPath("$.data[0].message").value("스레드와 프로세스의 차이점은 무엇인가요?"))
                        .andExpect(jsonPath("$.data[0].score").doesNotExist())
                        .andExpect(jsonPath("$.data[0].type").value("question"))
                        .andExpect(jsonPath("$.data[0].createdAt").doesNotExist())
                        .andExpect(jsonPath("$.data[1].message").value("스레드는 프로세스 내에서 실행되는 작업의 단위이고, 프로세스는 실행 중인 프로그램의 인스턴스입니다."))
                        .andExpect(jsonPath("$.data[1].score").value(20))
                        .andExpect(jsonPath("$.data[1].type").value("answer"))
                        .andExpect(jsonPath("$.data[1].createdAt").value("2024-08-15T21:00:00"))
                        .andExpect(jsonPath("$.data[2].message").value("그렇다면 멀티스레드와 멀티프로세스의 차이점은 무엇인가요?"))
                        .andExpect(jsonPath("$.data[2].score").doesNotExist())
                        .andExpect(jsonPath("$.data[2].type").value("question"))
                        .andExpect(jsonPath("$.data[2].createdAt").doesNotExist())
                        .andDo(
                            document(
                                identifier = "get-chat-message",
                                description = "채팅 메시지 내역 조회",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer 토큰"),
                                        ),
                                        responseFields(
                                            fieldWithPath("success").description("성공 여부").type(JsonFieldType.BOOLEAN),
                                            fieldWithPath("data[].message").description("메시지").type(JsonFieldType.STRING),
                                            fieldWithPath("data[].score")
                                                .description("점수")
                                                .type(JsonFieldType.NUMBER)
                                                .optional(),
                                            fieldWithPath("data[].type").description("채팅 타입").type(JsonFieldType.STRING),
                                            fieldWithPath("data[].createdAt")
                                                .description("생성일")
                                                .type(JsonFieldType.STRING)
                                                .optional(),
                                            fieldWithPath("error")
                                                .description("에러 정보")
                                                .type(JsonFieldType.OBJECT)
                                                .optional(),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("subjectId가 존재하지 않을 경우") {
                val id = 99999
                subjectQuery = NotExistingIdSubjectQuery()

                it("404 NOT_FOUND를 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v1/subjects/$id/chats")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isNotFound)
                        .andDo(
                            document(
                                identifier = "get-chat-message-not-found",
                                description = "존재하지 않는 주제 조회",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer 토큰"),
                                        ),
                                    ),
                            ),
                        )
                }
            }
        }

        describe("POST /api/v2/subjects/{subjectId}/chats/archive 요청은") {

            context("subjectId와 답변 채팅이 존재할 경우") {
                val id = 1
                subjectQuery = ExistIdSubjectQuery()
                chatQuery = ExistingAnswerChatQuery()

                it("201과 생성한 ID를 반환한다") {
                    mockMvc
                        .perform(
                            post("/api/v2/subjects/$id/chats/archive")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isCreated)
                        .andExpect(MockMvcResultMatchers.header().string("Location", "/api/v1/chat/archives/$id"))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.error").doesNotExist())
                        .andExpect(jsonPath("$.data").doesNotExist())
                        .andDo(
                            document(
                                identifier = "clear-chat",
                                description = "채팅 내역 초기화",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer 토큰"),
                                        ),
                                        responseHeaders(
                                            headerWithName("Location").description("생성된 리소스 URL"),
                                        ),
                                        responseFields(
                                            fieldWithPath("success").description("성공 상태 여부").type(JsonFieldType.BOOLEAN),
                                            fieldWithPath("error").description("에러 시 에러 정보").type(JsonFieldType.NULL),
                                            fieldWithPath("data").description("데이터").type(JsonFieldType.NULL),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("subjectId가 존재하지 않을 경우") {
                val id = 99999
                subjectQuery = NotExistingIdSubjectQuery()

                it("404 NOT_FOUND를 반환한다") {
                    mockMvc
                        .perform(
                            post("/api/v2/subjects/$id/chats/archive")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isNotFound)
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.data").doesNotExist())
                        .andExpect(jsonPath("$.error.message").value("Subject not found by id: $id"))
                        .andExpect(jsonPath("$.error.status").value(404))
                        .andExpect(jsonPath("$.error.code").value("SUBJECT_NOT_FOUND"))
                        .andDo(
                            document(
                                identifier = "clear-chat-not-found",
                                description = "존재하지 않는 주제 초기화",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer 토큰"),
                                        ),
                                        responseFields(
                                            fieldWithPath("success").description("상태").type(JsonFieldType.BOOLEAN),
                                            fieldWithPath("error.code").description("에러 코드").type(JsonFieldType.STRING),
                                            fieldWithPath("error.status")
                                                .description("HTTP 상태 코드")
                                                .type(JsonFieldType.NUMBER),
                                            fieldWithPath("error.message").description("에러 메시지").type(JsonFieldType.STRING),
                                            fieldWithPath("data").description("데이터").type(JsonFieldType.NULL),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("답변이 0일 경우") {
                val id = 2
                subjectQuery = ExistIdSubjectQuery()
                chatQuery = NoAnswerChatQuery()

                it("400 BAD_REQUEST를 반환한다") {
                    mockMvc
                        .perform(
                            post("/api/v2/subjects/$id/chats/archive")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isBadRequest)
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.data").doesNotExist())
                        .andExpect(jsonPath("$.error.message").value("제출한 답변이 없습니다."))
                        .andExpect(jsonPath("$.error.status").value(400))
                        .andExpect(jsonPath("$.error.code").value("NO_ANSWER"))
                        .andDo(
                            document(
                                identifier = "clear-chat-no-answer",
                                description = "답변이 없는 주제 초기화",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer 토큰"),
                                        ),
                                        responseFields(
                                            fieldWithPath("success").description("상태").type(JsonFieldType.BOOLEAN),
                                            fieldWithPath("error.code").description("에러 코드").type(JsonFieldType.STRING),
                                            fieldWithPath("error.status")
                                                .description("HTTP 상태 코드")
                                                .type(JsonFieldType.NUMBER),
                                            fieldWithPath("error.message").description("에러 메시지").type(JsonFieldType.STRING),
                                            fieldWithPath("data").description("데이터").type(JsonFieldType.NULL),
                                        ),
                                    ),
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

    class StubChatArchiveUseCase : ChatArchiveUseCase {
        override fun archive(
            jpaChats: List<JpaChat>,
            jpaMember: JpaMember,
            jpaSubject: JpaSubject,
        ): Long {
            val answerChatCount = jpaChats.count { it.isAnswer() }
            return when (answerChatCount) {
                0 -> throw NoAnswerException()
                else -> 1
            }
        }
    }

    class StubChatAnswerUseCase : ChatAnswerUseCase {
        override fun answer(command: CreateSubjectAnswerCommand): Flux<ChatResponse> =
            Flux.just(
                ChatResponse(
                    listOf(Generation("What is OS? (answer: ${command.answer})")),
                ),
            )
    }

    abstract class StubSubjectQuery : SubjectQuery {
        override fun findByCategory(category: String): List<JpaSubject> = throw NotImplementedError()

        override fun findWithMember(
            jpaMember: JpaMember,
            category: String?,
        ): List<MemberSubjectResponse> = throw NotImplementedError()

        override fun findByIdOrThrow(id: Long): JpaSubject = throw NotImplementedError()
    }

    class ExistIdSubjectQuery : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): JpaSubject =
            JpaSubject.createOS(id = id, title = "test subject", question = "test question")
    }

    class ExistCategorySubjectQueryStub : StubSubjectQuery() {
        override fun findByCategory(category: String): List<JpaSubject> =
            when (category) {
                "os" -> listOf(JpaSubject.createOS(id = 1, title = "OS", question = "What is OS?"))
                else -> throw SubjectCategoryNotFoundException("No such enum constant $category")
            }
    }

    class DummyChatQuery : ChatQuery {
        override fun findBySubjectAndMember(
            jpaSubject: JpaSubject,
            jpaMember: JpaMember,
        ): List<JpaChat> = throw NotImplementedError()
    }

    class NotExistCategorySubjectQuery : StubSubjectQuery() {
        override fun findByCategory(category: String) = throw SubjectCategoryNotFoundException(category)
    }

    class NotExistIdSubjectQuery : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): JpaSubject = throw SubjectNotFoundException(id)
    }

    class DummySubjectQuery : SubjectQuery {
        override fun findByCategory(category: String): List<JpaSubject> = throw NotImplementedError()

        override fun findWithMember(
            jpaMember: JpaMember,
            category: String?,
        ): List<MemberSubjectResponse> = throw NotImplementedError()

        override fun findByIdOrThrow(id: Long): JpaSubject = throw NotImplementedError()
    }

    class NoCategoryMemberSubjectQuery : StubSubjectQuery() {
        override fun findWithMember(
            jpaMember: JpaMember,
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
            jpaMember: JpaMember,
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

    class ExistingAnswerChatQuery : ChatQuery {
        override fun findBySubjectAndMember(
            jpaSubject: JpaSubject,
            jpaMember: JpaMember,
        ): List<JpaChat> =
            listOf(
                JpaChat.createQuestion(
                    jpaSubject = jpaSubject,
                    jpaMember = jpaMember,
                    message = "스레드와 프로세스의 차이점은 무엇인가요?",
                ),
                JpaChat.createAnswer(
                    jpaSubject = jpaSubject,
                    jpaMember = jpaMember,
                    answer = "스레드는 프로세스 내에서 실행되는 작업의 단위이고, 프로세스는 실행 중인 프로그램의 인스턴스입니다.",
                    score = 100,
                ),
            )
    }

    class NotExistingIdSubjectQuery : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): JpaSubject = throw SubjectNotFoundException(id)
    }

    class NoAnswerChatQuery : ChatQuery {
        override fun findBySubjectAndMember(
            jpaSubject: JpaSubject,
            jpaMember: JpaMember,
        ): List<JpaChat> =
            listOf(
                JpaChat.createQuestion(
                    jpaSubject = jpaSubject,
                    jpaMember = jpaMember,
                    message = "스레드와 프로세스의 차이점은 무엇인가요?",
                ),
            )
    }

    class ExistingSubjectIdChatQuery(
        private val date: LocalDateTime? = null,
    ) : ChatQuery {
        override fun findBySubjectAndMember(
            jpaSubject: JpaSubject,
            jpaMember: JpaMember,
        ): List<JpaChat> =
            listOf(
                JpaChat.createQuestion(
                    jpaSubject = jpaSubject,
                    jpaMember = jpaMember,
                    message = "스레드와 프로세스의 차이점은 무엇인가요?",
                ),
                JpaChat.createAnswer(
                    jpaSubject = jpaSubject,
                    jpaMember = jpaMember,
                    answer = "스레드는 프로세스 내에서 실행되는 작업의 단위이고, 프로세스는 실행 중인 프로그램의 인스턴스입니다.",
                    score = 20,
                    createdAt = date,
                ),
                JpaChat.createQuestion(
                    jpaSubject = jpaSubject,
                    jpaMember = jpaMember,
                    message = "그렇다면 멀티스레드와 멀티프로세스의 차이점은 무엇인가요?",
                ),
            )
    }
}
