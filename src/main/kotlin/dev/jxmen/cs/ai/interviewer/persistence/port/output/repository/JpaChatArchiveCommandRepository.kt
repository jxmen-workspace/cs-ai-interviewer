package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatArchiveCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatArchiveCommandRepository :
    ChatArchiveCommandRepository,
    JpaRepository<JpaChatArchive, Long>
