package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.persistence.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.persistence.JpaChatArchiveContent

interface ChatArchiveContentQueryRepository {
    fun findByArchive(jpaChatArchive: JpaChatArchive): List<JpaChatArchiveContent>
}
