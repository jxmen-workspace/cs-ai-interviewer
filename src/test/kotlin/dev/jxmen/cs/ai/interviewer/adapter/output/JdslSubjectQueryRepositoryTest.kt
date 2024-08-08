package dev.jxmen.cs.ai.interviewer.adapter.output

import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JdslSubjectQueryRepository
import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JpaChatCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.member.MemberCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCommandRepository
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class JdslSubjectQueryRepositoryTest(
    private val entityManager: EntityManager,
    private val subjectCommandRepository: SubjectCommandRepository,
    private val chatCommandRepository: JpaChatCommandRepository,
    private val memberCommandRepository: MemberCommandRepository,
) {
    private lateinit var subjectQueryRepository: SubjectQueryRepository

    private lateinit var subject1: Subject
    private lateinit var subject2: Subject
    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        subjectQueryRepository = JdslSubjectQueryRepository(entityManager)

        subject1 =
            subjectCommandRepository.save(
                Subject(
                    title = "test1",
                    question = "test1",
                    category = SubjectCategory.OS,
                ),
            )
        subject2 =
            subjectCommandRepository.save(
                Subject(
                    title = "test2",
                    question = "test2",
                    category = SubjectCategory.NETWORK,
                ),
            )

        member = memberCommandRepository.save(Member.createGoogleMember(name = "박주영", email = "me@jxmen.dev"))
        chatCommandRepository.saveAll(
            listOf(
                // subject1
                Chat.createAnswer(subject = subject1, member = member, answer = "test", score = 50),
                Chat.createQuestion(subject = subject1, member = member, nextQuestion = "test"),
                Chat.createAnswer(subject = subject1, member = member, answer = "test", score = 100),
                // subject2
                Chat.createAnswer(subject = subject2, member = member, answer = "test", score = 70),
            ),
        )
    }

    @Test
    fun findByCategory() {
        val findByCategory = subjectQueryRepository.findByCategory(SubjectCategory.OS)

        findByCategory.size shouldBe 1
        findByCategory[0].id shouldBe subject1.id
        findByCategory[0].title shouldBe "test1"
        findByCategory[0].question shouldBe "test1"
        findByCategory[0].category shouldBe SubjectCategory.OS
    }

    @Test
    fun findByCategoryNotExist() {
        listOf(SubjectCategory.NETWORK, SubjectCategory.DSA, SubjectCategory.DATABASE).forEach {
            val findByCategory = subjectQueryRepository.findByCategory(SubjectCategory.DSA)

            findByCategory.size shouldBe 0
        }
    }

    @Test
    fun findById() {
        subjectQueryRepository.findByIdOrNull(subject1.id)?.let {
            it.id shouldBe subject1.id
            it.title shouldBe "test1"
            it.category shouldBe SubjectCategory.OS
            it.question shouldBe "test1"
        } ?: throw Exception("Subject not found")
    }

    @Test
    fun findByIdNotExist() {
        subjectQueryRepository.findByIdOrNull(-1) shouldBe null
        subjectQueryRepository.findByIdOrNull(99999) shouldBe null
    }

    @Test
    fun findWithMember() {
        val findWithMember =
            subjectQueryRepository.findWithMember(
                member,
                null,
            )

        findWithMember.size shouldBe 2
        with(findWithMember[0]) {
            id shouldBe subject1.id
            title shouldBe "test1"
            category shouldBe SubjectCategory.OS
            maxScore shouldBe 100
        }
        with(findWithMember[1]) {
            id shouldBe subject2.id
            title shouldBe "test2"
            category shouldBe SubjectCategory.NETWORK
            maxScore shouldBe 70
        }
    }
}
