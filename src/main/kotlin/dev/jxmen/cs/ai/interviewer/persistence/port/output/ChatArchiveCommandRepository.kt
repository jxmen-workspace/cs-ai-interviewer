package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive

interface ChatArchiveCommandRepository {
    fun save(chatArchive: JpaChatArchive): JpaChatArchive
}
