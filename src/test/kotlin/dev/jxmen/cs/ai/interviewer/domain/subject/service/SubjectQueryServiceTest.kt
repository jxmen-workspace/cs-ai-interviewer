package dev.jxmen.cs.ai.interviewer.domain.subject.service

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.WithDataTestName
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import java.util.Optional
import java.util.function.Function

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
        val subjectQueryService = SubjectQueryService(StubSubjectRepository())

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

    override fun <S : Subject?> save(entity: S & Any): S & Any = TODO("Not yet implemented")

    override fun <S : Subject?> saveAll(entities: MutableIterable<S>): MutableList<S> = TODO("Not yet implemented")

    override fun <S : Subject?> findAll(example: Example<S>): MutableList<S> = TODO("Not yet implemented")

    override fun <S : Subject?> findAll(
        example: Example<S>,
        sort: Sort,
    ): MutableList<S> = TODO("Not yet implemented")

    override fun findAll(): MutableList<Subject> = TODO("Not yet implemented")

    override fun findAll(sort: Sort): MutableList<Subject> = TODO("Not yet implemented")

    override fun findAll(pageable: Pageable): Page<Subject> = TODO("Not yet implemented")

    override fun <S : Subject?> findAll(
        example: Example<S>,
        pageable: Pageable,
    ): Page<S> = TODO("Not yet implemented")

    override fun findAllById(ids: MutableIterable<Long>): MutableList<Subject> = TODO("Not yet implemented")

    override fun count(): Long = TODO("Not yet implemented")

    override fun <S : Subject?> count(example: Example<S>): Long = TODO("Not yet implemented")

    override fun delete(entity: Subject) = TODO("Not yet implemented")

    override fun deleteAllById(ids: MutableIterable<Long>) = TODO("Not yet implemented")

    override fun deleteAll(entities: MutableIterable<Subject>) = TODO("Not yet implemented")

    override fun deleteAll() = TODO("Not yet implemented")

    override fun <S : Subject?> findOne(example: Example<S>): Optional<S> = TODO("Not yet implemented")

    override fun <S : Subject?> exists(example: Example<S>): Boolean = TODO("Not yet implemented")

    override fun <S : Subject?, R : Any?> findBy(
        example: Example<S>,
        queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>,
    ): R & Any = TODO("Not yet implemented")

    override fun flush() = TODO("Not yet implemented")

    override fun <S : Subject?> saveAndFlush(entity: S & Any): S & Any = TODO("Not yet implemented")

    override fun <S : Subject?> saveAllAndFlush(entities: MutableIterable<S>): MutableList<S> = TODO("Not yet implemented")

    override fun deleteAllInBatch(entities: MutableIterable<Subject>) = TODO("Not yet implemented")

    override fun deleteAllInBatch() = TODO("Not yet implemented")

    override fun deleteAllByIdInBatch(ids: MutableIterable<Long>) = TODO("Not yet implemented")

    override fun getReferenceById(id: Long): Subject = TODO("Not yet implemented")

    override fun getById(id: Long): Subject = TODO("Not yet implemented")

    override fun getOne(id: Long): Subject = TODO("Not yet implemented")

    override fun deleteById(id: Long) = TODO("Not yet implemented")

    override fun existsById(id: Long): Boolean = TODO("Not yet implemented")

    override fun findById(id: Long): Optional<Subject> = TODO("Not yet implemented")
}
