package dev.jxmen.cs.ai.interviewer.persistence.port.output.query

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import dev.jxmen.cs.ai.interviewer.common.config.KotlinJdslConfig
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatCommandRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.MemberCommandRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectCommandRepository
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectQueryRepository
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
@Import(KotlinJdslConfig::class) // NOTE: DataJpaTest는 KotlinJdslConfig를 찾지 못함
class JdslSubjectQueryRepositoryTest(
    private val entityManager: EntityManager,
    private val subjectCommandRepository: SubjectCommandRepository,
    private val chatCommandRepository: ChatCommandRepository,
    private val memberCommandRepository: MemberCommandRepository,
    private val context: JpqlRenderContext,
) {
    private lateinit var subjectQueryRepository: SubjectQueryRepository
    private lateinit var jpaSubject1: JpaSubject
    private lateinit var jpaSubject2: JpaSubject
    private lateinit var jpaMember: JpaMember

    @BeforeEach
    fun setUp() {
        subjectQueryRepository = JdslSubjectQueryRepository(entityManager, context)

        jpaSubject1 =
            subjectCommandRepository.save(
                JpaSubject(
                    title = "test1",
                    question = "test1",
                    category = SubjectCategory.OS,
                ),
            )
        jpaSubject2 =
            subjectCommandRepository.save(
                JpaSubject(
                    title = "test2",
                    question = "test2",
                    category = SubjectCategory.NETWORK,
                ),
            )

        jpaMember = memberCommandRepository.save(JpaMember.createGoogleMember(name = "박주영", email = "me@jxmen.dev"))
        chatCommandRepository.saveAll(
            listOf(
                // subject1
                JpaChat.createAnswer(jpaSubject = jpaSubject1, jpaMember = jpaMember, answer = "test", score = 50),
                JpaChat.createQuestion(jpaSubject = jpaSubject1, jpaMember = jpaMember, message = "test"),
                JpaChat.createAnswer(jpaSubject = jpaSubject1, jpaMember = jpaMember, answer = "test", score = 100),
                // subject2
                JpaChat.createAnswer(jpaSubject = jpaSubject2, jpaMember = jpaMember, answer = "test", score = 70),
            ),
        )
    }

    @Test
    fun findByCategory() {
        val findByCategory = subjectQueryRepository.findByCategory(SubjectCategory.OS)

        findByCategory.size shouldBe 1
        findByCategory[0].id shouldBe jpaSubject1.id
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
        subjectQueryRepository.findByIdOrNull(jpaSubject1.id)?.let {
            it.id shouldBe jpaSubject1.id
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
            subjectQueryRepository.findWithMember(jpaMember)

        findWithMember.size shouldBe 2
        with(findWithMember[0]) {
            id shouldBe jpaSubject1.id
            title shouldBe "test1"
            category shouldBe SubjectCategory.OS
            maxScore shouldBe 100
        }
        with(findWithMember[1]) {
            id shouldBe jpaSubject2.id
            title shouldBe "test2"
            category shouldBe SubjectCategory.NETWORK
            maxScore shouldBe 70
        }
    }

    @Test
    fun findWithMemberCategory() {
        val findWithMember =
            subjectQueryRepository.findWithMember(
                jpaMember,
                SubjectCategory.OS,
            )

        findWithMember.size shouldBe 1
        with(findWithMember[0]) {
            id shouldBe jpaSubject1.id
            title shouldBe "test1"
            category shouldBe SubjectCategory.OS
            maxScore shouldBe 100
        }
    }
}
