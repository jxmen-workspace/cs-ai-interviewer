package dev.jxmen.cs.ai.interviewer.domain.subject.service

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
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
    override fun dataTestName() = "test case - '$input'"
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
                        subjectQueryService.getSubjectsByCategory(tc.input)
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
                    val subjectsByCategory = subjectQueryService.getSubjectsByCategory(tc.input)
                    subjectsByCategory.size shouldBe 1
                    with(subjectsByCategory[0]) {
                        title shouldBe tc.input.uppercase()
                        category shouldBe SubjectCategory.valueOf(tc.input.uppercase())
                    }
                }
            }
        }
    })

class StubSubjectQueryRepository : SubjectQueryRepository {
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

    override fun findById(id: Long): Optional<Subject> = Optional.empty()
}
