package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JpaChatArchiveContent

interface ChatArchiveContentCommandRepository {
    fun save(jpaChatArchiveContent: JpaChatArchiveContent): JpaChatArchiveContent
}
