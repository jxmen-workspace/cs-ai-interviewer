package dev.jxmen.cs.ai.interviewer.domain.subject.service

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class SubjectServiceTest :
    DescribeSpec({
        val subjectService = SubjectService(StubSubjectRepository())

        describe("getSubjectsByCategory") {
            context("만약 카테고리가 잘못된 값이라면") {
                it("IllegalArgumentException을 던져야 한다") {
                    shouldThrow<IllegalArgumentException> {
                        subjectService.getSubjectsByCategory("dsa1")
                    }
                }
            }
            context("만약 카테고리가 올바른 값이라면") {
                it("해당 카테고리의 주제를 반환해야 한다") {
                    val categories =
                        listOf(
                            Pair("dsa", SubjectCategory.DSA),
                            Pair("network", SubjectCategory.NETWORK),
                            Pair("database", SubjectCategory.DATABASE),
                            Pair("os", SubjectCategory.OS),
                        )

                    categories.forEach { (categoryStr, categoryEnum) ->
                        val subjectsByCategory = subjectService.getSubjectsByCategory(categoryStr)
                        subjectsByCategory.size shouldBe 1
                        with(subjectsByCategory[0]) {
                            title shouldBe categoryEnum.name
                            category shouldBe categoryEnum
                        }
                    }
                }
            }
        }
    })

class StubSubjectRepository : SubjectRepository {
    override fun findByCategory(category: SubjectCategory): List<Subject> =
        when (category) {
            SubjectCategory.DSA -> listOf(Subject(title = "DSA", question = "What is DSA?", category = SubjectCategory.DSA))
            SubjectCategory.NETWORK -> listOf(Subject(title = "NETWORK", question = "What is Network?", category = SubjectCategory.NETWORK))
            SubjectCategory.DATABASE ->
                listOf(
                    Subject(title = "DATABASE", question = "What is Database?", category = SubjectCategory.DATABASE),
                )
            SubjectCategory.OS -> listOf(Subject(title = "OS", question = "What is OS?", category = SubjectCategory.OS))
        }
}
