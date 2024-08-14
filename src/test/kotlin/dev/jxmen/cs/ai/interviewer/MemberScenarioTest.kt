package dev.jxmen.cs.ai.interviewer

import dev.jxmen.cs.ai.interviewer.application.port.output.AIApiClient
import dev.jxmen.cs.ai.interviewer.application.port.output.dto.AiApiAnswerResponse
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MemberCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCommandRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.willReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class MemberScenarioTest {
    @MockBean // 해당 빈만 모킹해서 사용한다.
    private lateinit var aiApiClient: AIApiClient

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var subjectCommandRepository: SubjectCommandRepository

    @Autowired
    private lateinit var memberCommandRepository: MemberCommandRepository

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        // kotlin에서는 버그로 인해 필터 추가 불가 - https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework/server-filters.html
        mockMvc =
            MockMvcBuilders
                .webAppContextSetup(context)
                .build()
    }

    @Test
    fun `멤버 답변 및 채팅 내역 시나리오 테스트`() {
        // 멤버 생성
        val testMember = Member.createGoogleMember(name = "박주영", email = "me@jxmen.dev")
        val createdMember = memberCommandRepository.save(testMember)

        // 멤버 인증 정보 저장
        val oauth2User = createOAuth2User(createdMember)
        val authentication = createOAuth2AuthenticationToken(oauth2User)
        SecurityContextHolder.getContext().authentication = authentication

        // 주제 생성
        val testSubject = Subject(title = "test subject", question = "test question", category = SubjectCategory.OS)
        val createdSubject = subjectCommandRepository.save(testSubject)

        // 주제 목록 조회
        mockMvc
            .get("/api/subjects") { param("category", "OS") }
            .andExpect {
                status { isOk() }
                jsonPath("$.data") { isArray() }
                jsonPath("$.data.length()") { value(1) }
                jsonPath("$.data[0].id") { value(createdSubject.id) }
                jsonPath("$.data[0].title") { value("test subject") }
                jsonPath("$.data[0].category") { value("OS") }
            }

        // 주제 상세 조회
        mockMvc
            .get("/api/subjects/${createdSubject.id}")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(createdSubject.id) }
                jsonPath("$.title") { value("test subject") }
                jsonPath("$.question") { value("test question") }
                jsonPath("$.category") { value("OS") }
            }

        // 채팅 API 조회 시 빈 값 응답 검증
        mockMvc
            .get("/api/v2/chat/messages?subjectId=${createdSubject.id}") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data") { isEmpty() }
            }

        // aiApiClient는 모킹한 정보를 리턴하도록 설정
        given { aiApiClient.requestAnswer(any(), any(), any()) }
            .willReturn {
                AiApiAnswerResponse(
                    nextQuestion = "next question",
                    score = 10,
                )
            }

        // 특정 주제에 대해 답변
        mockMvc
            .post("/api/v2/subjects/${createdSubject.id}/answer") {
                header("Authorization", "Bearer test-token")
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "answer": "test answer"
                    }
                    """.trimIndent()
            }.andExpect { status { isCreated() } }

        // 채팅 API 조회 - 답변과 다음 질문이 생성되었는지 검증
        mockMvc
            .get("/api/v2/chat/messages?subjectId=${createdSubject.id}") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data") { isNotEmpty() }
                jsonPath("$.data.length()") { value(2) }
                jsonPath("$.data[0].type") { value("answer") }
                jsonPath("$.data[0].message") { value("test answer") }
                jsonPath("$.data[0].score") { value(10) }
                jsonPath("$.data[1].type") { value("question") }
                jsonPath("$.data[1].message") { value("next question") }
                jsonPath("$.data[1].score") { value(null) }
            }

        // 채팅 아카이브
        mockMvc
            .post("/api/v1/subjects/${createdSubject.id}/chats/archive") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isCreated() }

                // NOTE: location 헤더 값은 알 수 없으므로 검증하지 않는다.
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { value(null) }
                jsonPath("$.error") { value(null) }
            }

        // 채팅 내역 재조회 - 아카이브 후 빈 값 응답 검증
        mockMvc
            .get("/api/v2/chat/messages?subjectId=${createdSubject.id}") {
                header("Authorization", "Bearer test-token")
            }.andExpect {
                status { isOk() }
                jsonPath("$.data") { isEmpty() }
            }

        // NOTE: 채팅 아카이브에 잘 저장이 되었는지 나중에 확인 필요
    }

    private fun createOAuth2AuthenticationToken(oauth2User: DefaultOAuth2User): OAuth2AuthenticationToken {
        val authentication =
            OAuth2AuthenticationToken(
                oauth2User,
                emptyList<GrantedAuthority>(),
                "google", // NOTE: 구글 외 다른 로그인 수단 추가 시 변경 필요
            )
        return authentication
    }

    private fun createOAuth2User(createdMember: Member): DefaultOAuth2User {
        val oauth2User =
            DefaultOAuth2User(
                emptyList<GrantedAuthority>(),
                mapOf(
                    "sub" to createdMember.id,
                    "name" to createdMember.name,
                    "email" to createdMember.email,
                ),
                "sub",
            )
        return oauth2User
    }
}
