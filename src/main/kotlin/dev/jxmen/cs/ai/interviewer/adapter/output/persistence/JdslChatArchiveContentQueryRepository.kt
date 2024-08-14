package dev.jxmen.cs.ai.interviewer.adapter.output.persistence

import com.linecorp.kotlinjdsl.dsl.jpql.jpql
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderer
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatArchiveContentQueryRepository
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Repository

@Repository
class JdslChatArchiveContentQueryRepository(
    private val entityManager: EntityManager,
    private val context: JpqlRenderContext,
) : ChatArchiveContentQueryRepository {
    private val renderer = JpqlRenderer()

    override fun findByArchive(jpaChatArchive: JpaChatArchive): List<JpaChatArchiveContent> {
        val rendered =
            renderer.render(
                jpql {
                    selectNew<JpaChatArchiveContent>(
                        path(JpaChatArchiveContent::archive),
                        path(JpaChatArchiveContent::content),
                    ).from(
                        entity(JpaChatArchiveContent::class),
                    ).where(
                        path(JpaChatArchiveContent::archive).eq(jpaChatArchive),
                    )
                },
                context,
            )

        return entityManager
            .createQuery(rendered.query, JpaChatArchiveContent::class.java)
            .apply {
                rendered.params.forEach { (name, value) ->
                    setParameter(name, value)
                }
            }.resultList
    }
}
