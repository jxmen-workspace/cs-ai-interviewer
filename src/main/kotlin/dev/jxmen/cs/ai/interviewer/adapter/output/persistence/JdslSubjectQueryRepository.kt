package dev.jxmen.cs.ai.interviewer.adapter.output.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import dev.jxmen.cs.ai.interviewer.adapter.input.dto.request.MemberSubjectResponse
import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.member.Member
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
    private val renderer = JpqlRenderer()

    override fun findByCategory(category: SubjectCategory): List<Subject> {
        val rendered =
            renderer.render(
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
                },
                context,
            )

        return entityManager
            .createQuery(rendered.query, Subject::class.java)
            .apply {
                rendered.params.forEach { (name, value) ->
                    setParameter(name, value)
                }
            }.resultList
    }

    override fun findByIdOrNull(id: Long): Subject? = entityManager.find(Subject::class.java, id)

    override fun findWithMember(
        member: Member,
        category: SubjectCategory?,
    ): List<MemberSubjectResponse> {
        val jpql =
            jpql {
                selectNew<MemberSubjectResponse>(
                    path(Subject::id),
                    path(Subject::title),
                    path(Subject::category),
                    max(path(Chat::score)),
                ).from(
                    entity(Subject::class),
                    leftJoin(Chat::class).on(
                        and(
                            path(Subject::id).eq(path(Chat::subject).path(Subject::id)),
                            path(Chat::member).eq(member),
                        ),
                    ),
                ).where(
                    category?.let { path(Subject::category).eq(it) },
                ).groupBy(
                    path(Subject::id),
                ).orderBy(
                    path(Subject::id).asc(),
                )
            }
        val rendered = renderer.render(jpql, context)

        return entityManager
            .createQuery(rendered.query, MemberSubjectResponse::class.java)
            .apply {
                rendered.params.forEach { (name, value) ->
                    setParameter(name, value)
                }
            }.resultList
    }
}
