package dev.jxmen.cs.ai.interviewer.application.adapter

import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.persistence.adapter.SubjectQueryService
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectQueryRepository
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

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
        val subjectQueryRepository = mockk<SubjectQueryRepository>()
        val subjectQueryService = SubjectQueryService(subjectQueryRepository)

        describe("findByByCategory") {
            context("만약 카테고리가 잘못된 값이라면 IllegalArgumentException을 던진다") {
                withData(
                    listOf(
                        TestCase("dsa1"),
                        TestCase(""),
                        TestCase(" "),
                        TestCase("  "),
                    ),
                ) { tc ->
                    every { subjectQueryRepository.findByCategory(any()) } returns emptyList()

                    shouldThrow<IllegalArgumentException> {
                        subjectQueryService.findByCategory(tc.input)
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
                    every { subjectQueryRepository.findByCategory(any()) } returns
                        listOf(
                            Subject(
                                title = tc.input.uppercase(),
                                question = "What is ${tc.input.uppercase()}?",
                                category = SubjectCategory.valueOf(tc.input.uppercase()),
                            ),
                        )

                    val subjectsByCategory = subjectQueryService.findByCategory(tc.input)
                    subjectsByCategory.size shouldBe 1
                    with(subjectsByCategory[0]) {
                        title shouldBe tc.input.uppercase()
                        category shouldBe SubjectCategory.valueOf(tc.input.uppercase())
                    }
                }
            }
        }

        describe("findById") {
            context("존재하는 id라면") {
                it("발견한 주제를 리턴한다.") {
                    every { subjectQueryRepository.findByIdOrNull(1L) } returns
                        Subject(
                            title = "OS",
                            question = "What is OS?",
                            category = SubjectCategory.OS,
                        )

                    shouldNotThrow<SubjectNotFoundException> {
                        val subject = subjectQueryService.findByIdOrThrow(1L)
                        subject.title shouldBe "OS"
                        subject.question shouldBe "What is OS?"
                        subject.category shouldBe SubjectCategory.OS
                    }
                }
            }
            context("존재하지 않는 id라면") {
                it("SubjectNotFoundException 예외를 던진다") {
                    every { subjectQueryRepository.findByIdOrNull(-1) } returns null

                    shouldThrow<SubjectNotFoundException> {
                        subjectQueryService.findByIdOrThrow(-1)
                    }
                }
            }
        }

        describe("findWithMember") {

            it("주제 정보와 멤버의 최대 점수 목록을 같이 리턴한다") {
                every { subjectQueryRepository.findWithMember(any(), any()) } returns
                    listOf(
                        MemberSubjectResponse(
                            id = 1,
                            title = "OS",
                            category = SubjectCategory.OS,
                            maxScore = 100,
                        ),
                    )

                val member = Member.createGoogleMember("박주영", "me@jxmen.dev")
                val memberSubjects: List<MemberSubjectResponse> = subjectQueryService.findWithMember(member)

                memberSubjects.size shouldBe 1
                with(memberSubjects[0]) {
                    id shouldBe 1
                    title shouldBe "OS"
                    category shouldBe SubjectCategory.OS
                    maxScore shouldBe 100
                }
            }
        }
    })
