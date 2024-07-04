package dev.jxmen.cs.ai.interviewer.domain.chat.api

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import dev.jxmen.cs.ai.interviewer.domain.chat.service.port.ChatQuery
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.domain.subject.service.port.SubjectQuery
import dev.jxmen.cs.ai.interviewer.global.GlobalControllerAdvice
import io.kotest.core.spec.style.DescribeSpec
import jakarta.servlet.http.Cookie
import org.springframework.mock.web.MockHttpSession
import org.springframework.restdocs.ManualRestDocumentation
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
        val stubChatQuery = StubChatQuery()
        val stubSubjectQuery = StubSubjectQuery()

        val manualRestDocumentation = ManualRestDocumentation()
        val controllerAdvice = GlobalControllerAdvice()

        lateinit var mockMvc: MockMvc

        beforeEach {
            val mockHttpSession = CustomMockHttpSession(StubChatQuery.EXIST_USER_SESSION_ID)

            mockMvc =
                MockMvcBuilders
                    .standaloneSetup(ChatApi(mockHttpSession, stubSubjectQuery, stubChatQuery))
                    .setControllerAdvice(controllerAdvice)
                    .apply<StandaloneMockMvcBuilder>(
                        MockMvcRestDocumentation.documentationConfiguration(
                            manualRestDocumentation,
                        ),
                    ).build()

            manualRestDocumentation.beforeTest(javaClass, javaClass.simpleName) // manual rest docs 사용시 필요
        }

        afterEach {
            manualRestDocumentation.afterTest() // manual rest docs 사용시 필요
        }

        describe("GET /api/chat/messages?subjectId={subjectId} 요청은") {

            context("subjectId와 userSessionId가 존재할경우") {
                it("200 OK와 Chat 객체를 반환한다") {
                    mockMvc
                        .perform(
                            get(
                                "/api/chat/messages?subjectId=${StubSubjectQuery.EXIST_SUBJECT_ID}",
                            ).cookie(Cookie("JSESSIONID", StubChatQuery.EXIST_USER_SESSION_ID)),
                        ).andExpect(status().isOk)
                        .andDo(
                            document(
                                identifier = "get-chat-message",
                                description = "채팅 메시지 내역 조회",
                                snippets =
                                    arrayOf(
                                        responseFields(
                                            fieldWithPath("data[].message").description("메시지").type(JsonFieldType.STRING),
                                            fieldWithPath("data[].score").description("점수").type(JsonFieldType.NULL),
                                            fieldWithPath("data[].type").description("채팅 타입").type(JsonFieldType.STRING),
                                        ),
                                    ),
                            ),
                        )
                }
            }
        }
    })

class CustomMockHttpSession(
    private val customSessionId: String,
) : MockHttpSession() {
    override fun getId(): String = customSessionId
}

class StubSubjectQuery : SubjectQuery {
    companion object {
        const val EXIST_SUBJECT_ID = 1L
    }

    override fun getSubjectsByCategory(cateStr: String): List<Subject> {
        TODO("Not yet implemented")
    }

    override fun getSubjectById(id: Long): Subject {
        if (id != EXIST_SUBJECT_ID) throw SubjectNotFoundException(id)

        return Subject(
            title = "스레드와 프로세스",
            question = "스레드와 프로세스의 차이점은 무엇인가요?",
            category = SubjectCategory.OS,
        )
    }
}

class StubChatQuery : ChatQuery {
    companion object {
        const val EXIST_USER_SESSION_ID = "1"
    }

    override fun findBySubjectAndUserSessionId(
        subject: Subject,
        userSessionId: String,
    ): List<Chat> {
        if (userSessionId != EXIST_USER_SESSION_ID) {
            return emptyList()
        }

        return listOf(
            Chat(
                subject = subject,
                chatType = ChatType.QUESTION,
                score = null,
                message = "스레드와 프로세스의 차이점은 무엇인가요?",
                userSessionId = userSessionId,
            ),
        )
    }
}
