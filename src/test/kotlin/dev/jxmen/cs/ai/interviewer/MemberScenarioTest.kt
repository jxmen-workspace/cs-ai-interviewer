package dev.jxmen.cs.ai.interviewer

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import dev.jxmen.cs.ai.interviewer.application.port.input.ChatAnswerUseCase
import dev.jxmen.cs.ai.interviewer.persistence.adapter.ChatAppender
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
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
import org.junit.jupiter.api.assertAll
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.willReturn
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
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
    private val fixtureMonkey = FixtureMonkey.builder().plugin(KotlinPlugin()).build()

    @MockBean
    lateinit var chatAnswerUseCase: ChatAnswerUseCase

    init {
        beforeEach {
            mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
            webTestClient = MockMvcWebTestClient.bindToApplicationContext(context).build()
        }

        describe("MemberScenarioTest") {
            val jpaSubject = subjectCommandRepository.save(fixtureMonkey.giveMeOne<JpaSubject>())

            context("인증되지 않은 사용자는") {

                it("공개된 API 요청 시 200을 응답한다") {
                    assertAll(
                        {
                            mockMvc
                                .get("/api/v1/subjects") { param("category", jpaSubject.category.name) }
                                .andExpect {
                                    status { isOk() }
                                    jsonPath("$.success") { value(true) }
                                    jsonPath("$.data") { isArray() }
                                    jsonPath("$.data.length()") { value(1) }
                                    jsonPath("$.data[0].id") { value(jpaSubject.id) }
                                    jsonPath("$.data[0].title") { value(jpaSubject.title) }
                                    jsonPath("$.data[0].category") { value(jpaSubject.category.name) }
                                    jsonPath("$.error") { value(null) }
                                }
                        },
                        {
                            mockMvc
                                .get("/api/v1/subjects/${jpaSubject.id}")
                                .andExpect {
                                    status { isOk() }
                                    jsonPath("$.success") { value(true) }
                                    jsonPath("$.data.id") { value(jpaSubject.id) }
                                    jsonPath("$.data.title") { value(jpaSubject.title) }
                                    jsonPath("$.data.question") { value(jpaSubject.question) }
                                    jsonPath("$.data.category") { value(jpaSubject.category.name) }
                                    jsonPath("$.error") { value(null) }
                                }
                        },
                    )
                }

                it("인증이 필요한 API 요청시 401을 응답한다") {
                    val apis =
                        listOf(
                            Pair(HttpMethod.GET, "/api/v1/subjects/my"),
                            Pair(HttpMethod.GET, "/api/v1/subjects/${jpaSubject.id}/chats"),
                            Pair(HttpMethod.POST, "/api/v2/subjects/${jpaSubject.id}/chats/archive"),
                            Pair(HttpMethod.GET, "/api/v5/subjects/${jpaSubject.id}/answer"),
                        )

                    apis.forEach { (method, url) ->
                        when (method) {
                            HttpMethod.GET -> mockMvc.get(url).andExpect { status { isUnauthorized() } }
                            HttpMethod.POST -> mockMvc.post(url).andExpect { status { isUnauthorized() } }
                            else -> throw IllegalArgumentException("Unsupported method: $method")
                        }
                    }
                }
            }

            context("인증된 사용자의 경우") {
                val score = 10
                val answer = "잘 모르겠습니다."
                val nextQuestion = "답변에 대한 점수: ${score}점"

                lateinit var jpaMember: JpaMember

                beforeEach {
                    jpaMember = fixtureMonkey.giveMeOne()
                    memberCommandRepository.save(jpaMember)
                    setAuthentication(jpaMember)

                    given { chatAnswerUseCase.answer(any()) }.willReturn {
                        Flux
                            .create<ChatResponse?> {
                                it.next(ChatResponse(listOf(Generation(answer))))
                                it.complete()
                            }.publishOn(Schedulers.boundedElastic())
                            .doOnComplete {
                                chatAppender.addAnswerAndNextQuestion(
                                    jpaSubject = jpaSubject,
                                    jpaMember = jpaMember,
                                    answer = answer,
                                    jpaChats = emptyList(),
                                    nextQuestion = nextQuestion,
                                )
                            }
                    }
                }

                it("멤버가 채팅을 하면 채팅이 저장된다") {
                    validateMySubjectHasValues(jpaSubject)
                    validateChatIsEmpty(jpaSubject)

                    answer(jpaSubject, answer)

                    validateChatSaved(jpaSubject, answer, nextQuestion, score)
                    validateMySubjectHasMaxScore(score)
                }

                it("멤버가 채팅을 초기화(아카이브)하면 채팅 내역이 지워지고, 아카이브에 저장된다") {
                    answer(jpaSubject, "test answer")

                    archive(jpaSubject)

                    validateChatIsEmpty(jpaSubject)
                    validateMySubjectHasNoMaxScore()

                    // NOTE: 추후 API 개발 시 아카이브 내용을 확인하는 API를 추가해야 한다.
                    val archives = chatArchiveQueryRepository.findBySubjectAndMember(jpaSubject, jpaMember)
                    archives.size shouldBe 1

                    val archiveContents = chatArchiveContentQueryRepository.findByArchive(archives[0])
                    archiveContents.size shouldBe 3
                }
            }
        }
    }

    private fun validateChatSaved(
        jpaSubject: JpaSubject,
        answer: String,
        nextQuestion: String,
        score: Int,
    ) {
        val now = LocalDateTime.now()
        mockMvc
            .get("/api/v1/subjects/${jpaSubject.id}/chats")
            .andExpect {
                status { isOk() }
                jsonPath("$.data") { haveLength(3) }
                jsonPath("$.data[0].type") { value("question") }
                jsonPath("$.data[0].message") { value(jpaSubject.question) }
                jsonPath("$.data[0].score") { value(null) }
                jsonPath("$.data[0].createdAt") { value(null) }
                jsonPath("$.data[1].type") { value("answer") }
                jsonPath("$.data[1].message") { value(answer) }
                jsonPath("$.data[1].score") { value(score) }
                jsonPath("$.data[1].createdAt") { value(matcher = BeforeDateMatcher(now)) }
                jsonPath("$.data[2].type") { value("question") }
                jsonPath("$.data[2].message") { value(nextQuestion) }
                jsonPath("$.data[2].score") { value(null) }
                jsonPath("$.data[2].createdAt") { value(null) }
            }
    }

    private fun validateMySubjectHasMaxScore(maxScore: Int) {
        mockMvc
            .get("/api/v1/subjects/my")
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].maxScore") { value(maxScore) }
            }
    }

    private fun validateMySubjectHasValues(jpaSubject: JpaSubject) {
        mockMvc
            .get("/api/v1/subjects/my")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { haveLength(1) }
                jsonPath("$.data[0].id") { value(jpaSubject.id) }
                jsonPath("$.data[0].title") { value(jpaSubject.title) }
                jsonPath("$.data[0].category") { value(jpaSubject.category.name) }
                jsonPath("$.data[0].maxScore") { value(null) }
                jsonPath("$.error") { value(null) }
            }
    }

    private fun validateMySubjectHasNoMaxScore() {
        mockMvc
            .get("/api/v1/subjects/my")
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].maxScore") { value(null) }
            }
    }

    private fun archive(jpaSubject: JpaSubject) {
        mockMvc
            .post("/api/v2/subjects/${jpaSubject.id}/chats/archive")
            .andExpect {
                status { isCreated() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { value(null) }
                jsonPath("$.error") { value(null) }
            }
    }

    private fun answer(
        jpaSubject: JpaSubject,
        answer: String,
    ) {
        webTestClient
            .get()
            .uri("/api/v5/subjects/{subjectId}/answer?message={message}", jpaSubject.id, answer)
            .exchange()
            .expectStatus()
            .isOk
    }

    private fun validateChatIsEmpty(jpaSubject: JpaSubject) {
        mockMvc
            .get("/api/v1/subjects/${jpaSubject.id}/chats")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { isEmpty() }
                jsonPath("$.error") { isEmpty() }
            }
    }

    private fun setAuthentication(jpaMember: JpaMember) {
        val oauth2User = createOAuth2User(jpaMember)
        val authentication = createOAuth2AuthenticationToken(oauth2User)
        SecurityContextHolder.getContext().authentication = authentication
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

    private fun createOAuth2User(createdJpaMember: JpaMember): DefaultOAuth2User =
        DefaultOAuth2User(
            emptyList<GrantedAuthority>(),
            mapOf(
                "sub" to createdJpaMember.id,
                "name" to createdJpaMember.name,
                "email" to createdJpaMember.email,
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
