package dev.jxmen.cs.ai.interviewer.domain.chat

import dev.jxmen.cs.ai.interviewer.adapter.output.persistence.JpaChatArchive

interface ChatArchiveCommandRepository {
    fun save(chatArchive: JpaChatArchive): JpaChatArchive
}
