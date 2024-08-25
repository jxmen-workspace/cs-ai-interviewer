package dev.jxmen.cs.ai.interviewer

import dev.jxmen.cs.ai.interviewer.application.port.input.ReactiveMemberChatUseCase
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MockMemberArgumentResolver
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.persistence.adapter.ChatAppender
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatArchiveContentQueryRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatArchiveQueryRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberCommandRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectCommandRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.haveLength
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.willReturn
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@ActiveProfiles("test")
class MemberScenarioTest(
    private val context: WebApplicationContext,
    private val subjectCommandRepository: SubjectCommandRepository,
    private val memberCommandRepository: MemberCommandRepository,
    private val chatArchiveQueryRepository: ChatArchiveQueryRepository,
    private val chatArchiveContentQueryRepository: ChatArchiveContentQueryRepository,
    private val chatAppender: ChatAppender,
) : DescribeSpec() {
    override fun extensions() = listOf(SpringExtension)

    private lateinit var mockMvc: MockMvc
    private lateinit var webTestClient: WebTestClient

    @MockBean
    lateinit var reactiveMemberChatService: ReactiveMemberChatUseCase

    init {
        beforeEach {
            mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
            webTestClient = MockMvcWebTestClient.bindToApplicationContext(context).build()
        }

        describe("MemberScenarioTest") {
            it("멤버 답변 및 채팅 내역 시나리오 테스트") {
                val member = MockMemberArgumentResolver.member
                memberCommandRepository.save(member)

                val oauth2User = createOAuth2User(member)
                val authentication = createOAuth2AuthenticationToken(oauth2User)
                SecurityContextHolder.getContext().authentication = authentication

                val subject =
                    subjectCommandRepository.save(
                        Subject(
                            title = "test subject",
                            question = "test question",
                            category = SubjectCategory.OS,
                        ),
                    )

                mockMvc
                    .get("/api/v1/subjects") { param("category", "OS") }
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.success") { value(true) }
                        jsonPath("$.data") { isArray() }
                        jsonPath("$.data.length()") { value(1) }
                        jsonPath("$.data[0].id") { value(subject.id) }
                        jsonPath("$.data[0].title") { value("test subject") }
                        jsonPath("$.data[0].category") { value("OS") }
                        jsonPath("$.error") { value(null) }
                    }

                mockMvc
                    .get("/api/v1/subjects/${subject.id}")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.success") { value(true) }
                        jsonPath("$.data.id") { value(subject.id) }
                        jsonPath("$.data.title") { value("test subject") }
                        jsonPath("$.data.question") { value("test question") }
                        jsonPath("$.data.category") { value("OS") }
                        jsonPath("$.error") { value(null) }
                    }.andDo {
                        print()
                    }

                mockMvc
                    .get("/api/v1/subjects/my")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.success") { value(true) }
                        jsonPath("$.data") { haveLength(1) }
                        jsonPath("$.data[0].id") { value(subject.id) }
                        jsonPath("$.data[0].title") { value("test subject") }
                        jsonPath("$.data[0].category") { value("OS") }
                        jsonPath("$.data[0].maxScore") { value(null) }
                        jsonPath("$.error") { value(null) }
                    }

                mockMvc
                    .get("/api/v1/subjects/${subject.id}/chats")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.success") { value(true) }
                        jsonPath("$.data") { isEmpty() }
                        jsonPath("$.error") { isEmpty() }
                    }

                val answer = "test answer"
                val nextQuestion = "답변에 대한 점수: 10점"
                given { reactiveMemberChatService.answerAsync(any()) }.willReturn {
                    Flux
                        .create<ChatResponse?> {
                            it.next(ChatResponse(listOf(Generation(answer))))
                            it.complete()
                        }.publishOn(Schedulers.boundedElastic())
                        .doOnComplete {
                            chatAppender.addAnswerAndNextQuestion(
                                subject = subject,
                                member = member,
                                answer = answer,
                                chats = emptyList(),
                                nextQuestion = nextQuestion,
                            )
                        }
                }

                webTestClient
                    .get()
                    .uri("/api/v5/subjects/{subjectId}/answer?message={message}", subject.id, answer)
                    .exchange()
                    .expectStatus()
                    .isOk

                val now = LocalDateTime.now()
                mockMvc
                    .get("/api/v1/subjects/${subject.id}/chats")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.data") { haveLength(3) }
                        jsonPath("$.data[0].type") { value("question") }
                        jsonPath("$.data[0].message") {
                            value(
                                Subject(
                                    title = "test subject",
                                    question = "test question",
                                    category = SubjectCategory.OS,
                                ).question,
                            )
                        }
                        jsonPath("$.data[0].score") { value(null) }
                        jsonPath("$.data[0].createdAt") { value(null) }
                        jsonPath("$.data[1].type") { value("answer") }
                        jsonPath("$.data[1].message") { value(answer) }
                        jsonPath("$.data[1].score") { value(10) }
                        jsonPath("$.data[1].createdAt") { value(matcher = BeforeDateMatcher(now)) }
                        jsonPath("$.data[2].type") { value("question") }
                        jsonPath("$.data[2].message") { value(nextQuestion) }
                        jsonPath("$.data[2].score") { value(null) }
                        jsonPath("$.data[2].createdAt") { value(null) }
                    }

                mockMvc
                    .get("/api/v1/subjects/my")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.data[0].maxScore") { value(10) }
                    }

                mockMvc
                    .post("/api/v2/subjects/${subject.id}/chats/archive")
                    .andExpect {
                        status { isCreated() }
                        jsonPath("$.success") { value(true) }
                        jsonPath("$.data") { value(null) }
                        jsonPath("$.error") { value(null) }
                    }

                mockMvc
                    .get("/api/v1/subjects/${subject.id}/chats")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.data") { isEmpty() }
                    }

                mockMvc
                    .get("/api/v1/subjects/my")
                    .andExpect {
                        status { isOk() }
                        jsonPath("$.data[0].maxScore") { value(null) }
                    }

                val archives = chatArchiveQueryRepository.findBySubjectAndMember(subject, member)
                archives.size shouldBe 1

                val archiveContents = chatArchiveContentQueryRepository.findByArchive(archives[0])
                archiveContents.size shouldBe 3
            }
        }
    }

    private fun createOAuth2AuthenticationToken(
        oauth2User: DefaultOAuth2User,
        provider: String = "google",
    ): OAuth2AuthenticationToken =
        OAuth2AuthenticationToken(
            oauth2User,
            emptyList<GrantedAuthority>(),
            provider,
        )

    private fun createOAuth2User(createdMember: Member): DefaultOAuth2User =
        DefaultOAuth2User(
            emptyList<GrantedAuthority>(),
            mapOf(
                "sub" to createdMember.id,
                "name" to createdMember.name,
                "email" to createdMember.email,
            ),
            "sub",
        )
}

class BeforeDateMatcher(
    private val date: LocalDateTime,
) : BaseMatcher<LocalDateTime>() {
    override fun describeTo(description: Description?) {
        description?.appendText("date is $date")
    }

    override fun matches(actual: Any?): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(date)
    }
}
