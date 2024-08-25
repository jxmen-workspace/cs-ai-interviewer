package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import dev.jxmen.cs.ai.interviewer.domain.member.Member
import dev.jxmen.cs.ai.interviewer.domain.subject.Subject
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive
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
        subject: Subject,
        member: Member,
    ): List<JpaChatArchive> {
        val rendered =
            renderer.render(
                jpql {
                    selectNew<JpaChatArchive>(
                        path(JpaChatArchive::id),
                        path(JpaChatArchive::subject),
                        path(JpaChatArchive::member),
                    ).from(
                        entity(JpaChatArchive::class),
                    ).where(
                        and(
                            path(JpaChatArchive::subject).eq(subject),
                            path(JpaChatArchive::member).eq(member),
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
