package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import dev.jxmen.cs.ai.interviewer.domain.subject.SubjectCategory
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatContent
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.SubjectQueryRepository
import dev.jxmen.cs.ai.interviewer.presentation.dto.request.MemberSubjectResponse
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class JdslSubjectQueryRepository(
    private val entityManager: EntityManager,
    private val jpqlRenderContext: JpqlRenderContext,
) : SubjectQueryRepository {
    private val renderer = JpqlRenderer()

    override fun findByCategory(category: SubjectCategory): List<JpaSubject> {
        val rendered =
            renderer.render(
                jpql {
                    selectNew<JpaSubject>(
                        path(JpaSubject::id),
                        path(JpaSubject::title),
                        path(JpaSubject::question),
                        path(JpaSubject::category),
                    ).from(
                        entity(JpaSubject::class),
                    ).where(
                        path(JpaSubject::category).eq(category),
                    )
                },
                jpqlRenderContext,
            )

        return entityManager
            .createQuery(rendered.query, JpaSubject::class.java)
            .apply {
                rendered.params.forEach { (name, value) ->
                    setParameter(name, value)
                }
            }.resultList
    }

    override fun findByIdOrNull(id: Long): JpaSubject? = entityManager.find(JpaSubject::class.java, id)

    override fun findWithMember(
        jpaMember: JpaMember,
        category: SubjectCategory?,
    ): List<MemberSubjectResponse> {
        val jpql =
            jpql {
                selectNew<MemberSubjectResponse>(
                    path(JpaSubject::id),
                    path(JpaSubject::title),
                    path(JpaSubject::category),
                    max(path(JpaChat::content).path(JpaChatContent::score)),
                ).from(
                    entity(JpaSubject::class),
                    leftJoin(JpaChat::class).on(
                        and(
                            path(JpaSubject::id).eq(path(JpaChat::jpaSubject).path(JpaSubject::id)),
                            path(JpaChat::jpaMember).eq(jpaMember),
                        ),
                    ),
                ).where(
                    category?.let { path(JpaSubject::category).eq(it) },
                ).groupBy(
                    path(JpaSubject::id),
                ).orderBy(
                    path(JpaSubject::id).asc(),
                )
            }
        val rendered = renderer.render(jpql, jpqlRenderContext)

        return entityManager
            .createQuery(rendered.query, MemberSubjectResponse::class.java)
            .apply {
                rendered.params.forEach { (name, value) ->
                    setParameter(name, value)
                }
            }.resultList
    }
}
