package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchiveContent
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatArchiveContentCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatArchiveContentCommandRepository :
    ChatArchiveContentCommandRepository,
    JpaRepository<JpaChatArchiveContent, Long>
