package dev.jxmen.cs.ai.interviewer.persistence.port.output.query

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatArchiveQueryRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class JdslChatArchiveQueryRepository(
    private val entityManager: EntityManager,
    private val context: JpqlRenderContext,
) : ChatArchiveQueryRepository {
    private val renderer = JpqlRenderer()

    override fun findBySubjectAndMember(
        jpaSubject: JpaSubject,
        jpaMember: JpaMember,
    ): List<JpaChatArchive> {
        val rendered =
            renderer.render(
                jpql {
                    selectNew<JpaChatArchive>(
                        path(JpaChatArchive::id),
                        path(JpaChatArchive::jpaSubject),
                        path(JpaChatArchive::jpaMember),
                    ).from(
                        entity(JpaChatArchive::class),
                    ).where(
                        and(
                            path(JpaChatArchive::jpaSubject).eq(jpaSubject),
                            path(JpaChatArchive::jpaMember).eq(jpaMember),
                        ),
                    )
                },
                context,
            )

        return entityManager
            .createQuery(rendered.query, JpaChatArchive::class.java)
            .apply {
                rendered.params.forEach { (name, value) ->
                    setParameter(name, value)
                }
            }.resultList
    }
}
