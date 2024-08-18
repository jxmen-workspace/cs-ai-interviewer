package dev.jxmen.cs.ai.interviewer.adapter.input

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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

class ChatApiTest :
    DescribeSpec({
        lateinit var mockMvc: MockMvc

        lateinit var subjectQuery: StubSubjectQuery
        lateinit var chatQuery: ChatQuery

        beforeEach {
            mockMvc =
                MockMvcBuilders
                    .standaloneSetup(ChatApi(subjectQuery, chatQuery))
                    .setControllerAdvice(GlobalControllerAdvice())
                    .setCustomArgumentResolvers(MockMemberArgumentResolver())
                    .build()
        }

        describe("GET /api/v2/chat/messages?subjectId={subjectId} 요청은") {
            context("subjectId가 존재할경우") {
                val id = 1
                val date = LocalDateTime.of(2024, 8, 15, 21, 0, 0)
                subjectQuery = ExistingIdSubjectQueryStub()
                chatQuery = ExistingSubjectIdChatQueryStub(date)

                it("200 OK와 Chat 객체를 반환한다") {
                    mockMvc
                        .perform(
                            get("/api/v2/chat/messages?subjectId=$id")
                                .header("Authorization", "Bearer token"),
                        ).andExpect(status().isOk)
                        .andExpect(jsonPath("$.data[0].message").value("스레드와 프로세스의 차이점은 무엇인가요?"))
                        .andExpect(jsonPath("$.data[0].score").doesNotExist())
                        .andExpect(jsonPath("$.data[0].type").value("question"))
                        .andExpect(jsonPath("$.data[0].createdAt").doesNotExist())
                        .andExpect(jsonPath("$.data[1].message").value("스레드는 프로세스 내에서 실행되는 작업의 단위이고, 프로세스는 실행 중인 프로그램의 인스턴스입니다."))
                        .andExpect(jsonPath("$.data[1].score").value(100))
                        .andExpect(jsonPath("$.data[1].type").value("answer"))
                        .andExpect(jsonPath("$.data[1].createdAt").value("2024-08-15T21:00:00"))
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

    class ExistingSubjectIdChatQueryStub(
        private val date: LocalDateTime? = null,
    ) : ChatQuery {
        override fun findBySubjectAndMember(
            subject: Subject,
            member: Member,
        ): List<Chat> =
            listOf(
                Chat.createQuestion(
                    subject = subject,
                    member = member,
                    message = "스레드와 프로세스의 차이점은 무엇인가요?",
                ),
                Chat.createAnswer(
                    subject = subject,
                    member = member,
                    answer = "스레드는 프로세스 내에서 실행되는 작업의 단위이고, 프로세스는 실행 중인 프로그램의 인스턴스입니다.",
                    score = 100,
                    createdAt = date,
                ),
            )
    }

    class NotExistingIdSubjectQueryStub : StubSubjectQuery() {
        override fun findByIdOrThrow(id: Long): Subject = throw SubjectNotFoundException(id)

        override fun findByIdOrThrowV2(id: Long): Subject = throw SubjectNotFoundExceptionV2(id)
    }
}
