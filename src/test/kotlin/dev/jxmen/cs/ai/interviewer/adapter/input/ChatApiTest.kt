package dev.jxmen.cs.ai.interviewer.adapter.input

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatQuery
import dev.jxmen.cs.ai.interviewer.application.port.input.SubjectQuery
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MockMemberArgumentResolver
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

class ChatApiTest :
    DescribeSpec({
        val manualRestDocumentation = ManualRestDocumentation()

        lateinit var mockMvc: MockMvc

        beforeEach {
            mockMvc =
                MockMvcBuilders
                    .standaloneSetup(ChatApi(StubSubjectQuery(), StubChatQuery()))
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
            context("subjectId와 userSessionId가 존재할경우") {
                it("200 OK와 Chat 객체를 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v2/chat/messages?subjectId=${StubSubjectQuery.EXIST_SUBJECT_ID}")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isOk)
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
                                            fieldWithPath("data[].score").description("점수").type(JsonFieldType.NULL).optional(),
                                            fieldWithPath("data[].type").description("채팅 타입").type(JsonFieldType.STRING),
                                        ),
                                    ),
                            ),
                        )
                }
            }

            context("subjectId가 존재하지 않을 경우") {

                it("404 NOT_FOUND를 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v2/chat/messages?subjectId=9999999")
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

        class BACD
    })

class StubSubjectQuery : SubjectQuery {
    companion object {
        const val EXIST_SUBJECT_ID = 1L
    }

    override fun findBySubject(cateStr: String): List<Subject> {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): Subject {
        if (id != EXIST_SUBJECT_ID) throw SubjectNotFoundException(id)

        return Subject(
            title = "스레드와 프로세스",
            question = "스레드와 프로세스의 차이점은 무엇인가요?",
            category = SubjectCategory.OS,
        )
    }

    override fun findWithMember(
        member: Member,
        category: String?,
    ): List<MemberSubjectResponse> {
        TODO("Not yet implemented")
    }
}

class StubChatQuery : ChatQuery {
    companion object {
        const val EXIST_USER_SESSION_ID = "1"
    }

    override fun findBySubjectAndMember(
        subject: Subject,
        member: Member,
    ): List<Chat> =
        listOf(
            Chat(
                subject = subject,
                chatType = ChatType.QUESTION,
                score = null,
                message = "스레드와 프로세스의 차이점은 무엇인가요?",
                userSessionId = EXIST_USER_SESSION_ID,
            ),
        )
}
