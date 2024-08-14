package dev.jxmen.cs.ai.interviewer.adapter.input

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MockMemberArgumentResolver
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundExceptionV2
import dev.jxmen.cs.ai.interviewer.global.GlobalControllerAdvice
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

class ChatApiTest :
    DescribeSpec({
        val manualRestDocumentation = ManualRestDocumentation()
        lateinit var mockMvc: MockMvc

        lateinit var subjectQuery: StubSubjectQuery
        lateinit var chatQuery: ChatQuery

        beforeEach {
            mockMvc =
                MockMvcBuilders
                    .standaloneSetup(ChatApi(subjectQuery, chatQuery))
                    .setControllerAdvice(GlobalControllerAdvice())
                    .setCustomArgumentResolvers(MockMemberArgumentResolver())
                    .apply<StandaloneMockMvcBuilder>(
                        MockMvcRestDocumentation.documentationConfiguration(manualRestDocumentation),
                    ).build()

            manualRestDocumentation.beforeTest(javaClass, javaClass.simpleName) // manual rest docs 사용시 필요
        }

        afterEach {
            manualRestDocumentation.afterTest() // manual rest docs 사용시 필요
        }

        describe("GET /api/v2/chat/messages?subjectId={subjectId} 요청은") {
            context("subjectId가 존재할경우") {
                val id = 1
                subjectQuery = ExistingIdSubjectQueryStub()
                chatQuery = ExistingSubjectIdChatQueryStub()

                it("200 OK와 Chat 객체를 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v2/chat/messages?subjectId=$id")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.data[0].message").value("스레드와 프로세스의 차이점은 무엇인가요?"))
                        .andExpect(jsonPath("$.data[0].score").doesNotExist())
                        .andExpect(jsonPath("$.data[0].type").value("question"))
                        .andExpect(jsonPath("$.data[1].message").value("스레드는 프로세스 내에서 실행되는 작업의 단위이고, 프로세스는 실행 중인 프로그램의 인스턴스입니다."))
                        .andExpect(jsonPath("$.data[1].score").value(100))
                        .andExpect(jsonPath("$.data[1].type").value("answer"))
                        .andDo(
                            document(
                                identifier = "get-chat-message",
                                description = "채팅 메시지 내역 조회",
                                snippets =
                                    arrayOf(
                                        requestHeaders(
                                            headerWithName("Authorization").description("Bearer token"),
                                        ),
                                        responseFields(
                                            fieldWithPath("data[].message").description("메시지").type(JsonFieldType.STRING),
                                            fieldWithPath("data[].score")
                                                .description("점수")
                                                .type(JsonFieldType.NUMBER)
                                                .optional(),
                                            fieldWithPath("data[].type").description("채팅 타입").type(JsonFieldType.STRING),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("subjectId가 존재하지 않을 경우") {
                val id = 99999
                subjectQuery = NotExistingIdSubjectQueryStub()
                chatQuery = DummyChatQuery()

                it("404 NOT_FOUND를 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v2/chat/messages?subjectId=$id")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isNotFound)
                        .andDo(
                            document(
                                identifier = "get-chat-message-not-found",
                                description = "존재하지 않는 주제 조회",
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
    }) {
    abstract class StubSubjectQuery : SubjectQuery {
        override fun findByCategory(cateStr: String): List<Subject> = throw NotImplementedError()

        override fun findWithMember(
            member: Member,
            category: String?,
        ): List<MemberSubjectResponse> = throw NotImplementedError()

        abstract override fun findByIdOrThrow(id: Long): Subject

        abstract override fun findByIdOrThrowV2(id: Long): Subject
    }

    open class ExistingIdSubjectQueryStub : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): Subject =
            Subject(
                id = id,
                title = "스레드와 프로세스의 차이",
                question = "스레드와 프로세스의 차이점은 무엇인가요?",
                category = SubjectCategory.OS,
            )

        override fun findByIdOrThrowV2(id: Long): Subject = findByIdOrThrow(id)
    }

    class DummyChatQuery : ChatQuery {
        override fun findBySubjectAndMember(
            subject: Subject,
            member: Member,
        ): List<Chat> = emptyList()
    }

    class ExistingSubjectIdChatQueryStub : ChatQuery {
        override fun findBySubjectAndMember(
            subject: Subject,
            member: Member,
        ): List<Chat> =
            listOf(
                Chat.createQuestion(
                    subject = subject,
                    member = member,
                    nextQuestion = "스레드와 프로세스의 차이점은 무엇인가요?",
                ),
                Chat.createAnswer(
                    subject = subject,
                    member = member,
                    answer = "스레드는 프로세스 내에서 실행되는 작업의 단위이고, 프로세스는 실행 중인 프로그램의 인스턴스입니다.",
                    score = 100,
                ),
            )
    }

    class NotExistingIdSubjectQueryStub : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): Subject = throw SubjectNotFoundException(id)

        override fun findByIdOrThrowV2(id: Long): Subject = throw SubjectNotFoundExceptionV2(id)
    }
}
