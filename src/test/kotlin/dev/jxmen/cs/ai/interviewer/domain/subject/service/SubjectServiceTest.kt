package dev.jxmen.cs.ai.interviewer.domain.subject.service

import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import java.util.Optional
import java.util.function.Function

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
