package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JpaChatArchiveContent

interface ChatArchiveContentQueryRepository {
    fun findByArchive(jpaChatArchive: JpaChatArchive): List<JpaChatArchiveContent>
}
