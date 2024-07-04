package dev.jxmen.cs.ai.interviewer.domain.subject.service.adapter

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.util.Optional

/**
 * kotest custom test name (empty test name is not allowed.)
 *
 * https://kotest.io/docs/framework/datatesting/custom-test-names.html#withdatatestname
 */
data class TestCase(
    val input: String,
) : WithDataTestName {
    override fun dataTestName() = "case - '$input'"
}

class SubjectQueryServiceTest :
    DescribeSpec({
        val subjectQueryService = SubjectQueryService(StubSubjectQueryRepository())

        describe("getSubjectsByCategory") {
            context("만약 카테고리가 잘못된 값이라면 IllegalArgumentException을 던진다") {
                withData(
                    listOf(
                        TestCase("dsa1"),
                        TestCase(""),
                        TestCase(" "),
                        TestCase("  "),
                    ),
                ) { tc ->
                    shouldThrow<IllegalArgumentException> {
                        subjectQueryService.findBySubject(tc.input)
                    }
                }
            }
            context("만약 카테고리가 올바른 값이라면 해당 카테고리의 주제를 반환한다.") {
                withData(
                    listOf(
                        TestCase("dsa"),
                        TestCase("network"),
                        TestCase("database"),
                        TestCase("os"),
                    ),
                ) { tc ->
                    val subjectsByCategory = subjectQueryService.findBySubject(tc.input)
                    subjectsByCategory.size shouldBe 1
                    with(subjectsByCategory[0]) {
                        title shouldBe tc.input.uppercase()
                        category shouldBe SubjectCategory.valueOf(tc.input.uppercase())
                    }
                }
            }
        }

        describe("getSubjectById") {
            context("존재하는 id라면") {
                it("발견한 주제를 리턴한다.") {
                    shouldNotThrow<SubjectNotFoundException> {
                        val subject = subjectQueryService.findById(1L)
                        subject.title shouldBe "OS"
                        subject.question shouldBe "What is OS?"
                        subject.category shouldBe SubjectCategory.OS
                    }
                }
            }
            context("존재하지 않는 id라면") {
                it("SubjectNotFoundException 예외를 던진다") {
                    shouldThrow<SubjectNotFoundException> {
                        subjectQueryService.findById(StubSubjectQueryRepository.NOT_EXIST_ID)
                    }
                }
            }
        }
    })

class StubSubjectQueryRepository : SubjectQueryRepository {
    companion object {
        const val NOT_EXIST_ID = -1L
    }

    override fun findByCategory(category: SubjectCategory): List<Subject> =
        when (category) {
            SubjectCategory.DSA ->
                listOf(
                    Subject(title = "DSA", question = "What is DSA?", category = SubjectCategory.DSA),
                )
            SubjectCategory.NETWORK ->
                listOf(
                    Subject(title = "NETWORK", question = "What is Network?", category = SubjectCategory.NETWORK),
                )
            SubjectCategory.DATABASE ->
                listOf(
                    Subject(title = "DATABASE", question = "What is Database?", category = SubjectCategory.DATABASE),
                )
            SubjectCategory.OS ->
                listOf(
                    Subject(title = "OS", question = "What is OS?", category = SubjectCategory.OS),
                )
        }

    override fun findById(id: Long): Optional<Subject> {
        if (NOT_EXIST_ID == id) {
            return Optional.empty()
        }

        return Optional.of(
            Subject(title = "OS", question = "What is OS?", category = SubjectCategory.OS),
        )
    }
}
