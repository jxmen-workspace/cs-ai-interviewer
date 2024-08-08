package dev.jxmen.cs.ai.interviewer.adapter.output

import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JdslSubjectQueryRepository
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

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class JdslSubjectQueryRepositoryTest(
    private val entityManager: EntityManager,
    private val subjectCommandRepository: SubjectCommandRepository,
) {
    private var savedSubjectId: Long = 0

    private lateinit var subjectQueryRepository: SubjectQueryRepository

    @BeforeEach
    fun setUp() {
        subjectQueryRepository = JdslSubjectQueryRepository(entityManager)
        savedSubjectId =
            subjectCommandRepository
                .save(
                    Subject(
                        title = "test",
                        question = "test",
                        category = SubjectCategory.OS,
                    ),
                ).id
    }

    @Test
    fun findByCategory() {
        val findByCategory = subjectQueryRepository.findByCategory(SubjectCategory.OS)

        findByCategory.size shouldBe 1
        findByCategory[0].id shouldBe savedSubjectId
        findByCategory[0].title shouldBe "test"
        findByCategory[0].question shouldBe "test"
        findByCategory[0].category shouldBe SubjectCategory.OS
    }

    @Test
    fun findByCategoryNotExist() {
        listOf(SubjectCategory.NETWORK, SubjectCategory.DSA, SubjectCategory.DATABASE).forEach {
            val findByCategory = subjectQueryRepository.findByCategory(SubjectCategory.NETWORK)

            findByCategory.size shouldBe 0
        }
    }

    @Test
    fun findById() {
        subjectQueryRepository.findByIdOrNull(savedSubjectId)?.let {
            it.id shouldBe savedSubjectId
            it.title shouldBe "test"
            it.category shouldBe SubjectCategory.OS
            it.question shouldBe "test"
        } ?: throw Exception("Subject not found")
    }

    @Test
    fun findByIdNotExist() {
        subjectQueryRepository.findByIdOrNull(-1) shouldBe null
        subjectQueryRepository.findByIdOrNull(99999) shouldBe null
    }
}
