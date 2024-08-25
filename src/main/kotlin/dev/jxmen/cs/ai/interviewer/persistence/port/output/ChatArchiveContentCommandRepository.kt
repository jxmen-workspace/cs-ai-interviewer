package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchiveContent

interface ChatArchiveContentCommandRepository {
    fun save(jpaChatArchiveContent: JpaChatArchiveContent): JpaChatArchiveContent
}
