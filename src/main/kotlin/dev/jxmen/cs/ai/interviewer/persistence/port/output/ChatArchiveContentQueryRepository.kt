package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchiveContent

interface ChatArchiveContentQueryRepository {
    fun findByArchive(jpaChatArchive: JpaChatArchive): List<JpaChatArchiveContent>
}
