package dev.jxmen.cs.ai.interviewer.adapter.output.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectQueryRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class JdslSubjectQueryRepository(
    private val entityManager: EntityManager,
) : SubjectQueryRepository {
    private val context = JpqlRenderContext()

    override fun findByCategory(category: SubjectCategory): List<Subject> {
        val query =
            jpql {
                selectNew<Subject>(
                    path(Subject::id),
                    path(Subject::title),
                    path(Subject::question),
                    path(Subject::category),
                ).from(
                    entity(Subject::class),
                ).where(
                    path(Subject::category).eq(category),
                )
            }

        val renderer = JpqlRenderer()
        val rendered = renderer.render(query, context)

        val apply =
            entityManager.createQuery(rendered.query, Subject::class.java).apply {
                rendered.params.forEach { (name, value) ->
                    setParameter(name, value)
                }
            }

        return apply.resultList
    }

    override fun findByIdOrNull(id: Long): Subject? = entityManager.find(Subject::class.java, id)
}
