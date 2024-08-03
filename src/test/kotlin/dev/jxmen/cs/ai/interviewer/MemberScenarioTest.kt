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
import org.springframework.mock.web.MockHttpSession
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
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `멤버 답변 및 채팅 내역 시나리오 테스트`() {
        // 멤버 생성
        val testMember = Member.createGoogleMember(name = "test", email = "test@xample.com")
        val createdMember = memberCommandRepository.save(testMember)

        // 로그인 여부 API 조회
        mockMvc
            .get("/api/v1/is-logged-in")
            .andExpect {
                status { isOk() }
                jsonPath("$.isLoggedIn") { value(false) }
            }

        // 세션에 멤버 정보 저장 / mockMvc는 해당 세션 정보를 사용하도록 설정
        val mockHttpSession = MockHttpSession()
        mockHttpSession.setAttribute("member", createdMember)

        // 로그인 여부 API 조회
        mockMvc
            .get("/api/v1/is-logged-in") { session = mockHttpSession }
            .andExpect {
                status { isOk() }
                jsonPath("$.isLoggedIn") { value(true) }
            }

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
            .get("/api/v2/chat/messages?subjectId=${createdSubject.id}") { session = mockHttpSession }
            .andExpect {
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
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "answer": "test answer"
                    }
                    """.trimIndent()
                session = mockHttpSession
            }.andExpect { status { isCreated() } }

        // 채팅 API 조회 - 답변과 다음 질문이 생성되었는지 검증
        mockMvc
            .get("/api/v2/chat/messages?subjectId=${createdSubject.id}") { session = mockHttpSession }
            .andExpect {
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
    }
}