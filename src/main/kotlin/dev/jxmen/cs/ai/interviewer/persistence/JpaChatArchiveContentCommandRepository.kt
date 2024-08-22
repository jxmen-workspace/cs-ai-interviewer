package dev.jxmen.cs.ai.interviewer.persistence

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatArchiveContentCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatArchiveContentCommandRepository :
    ChatArchiveContentCommandRepository,
    JpaRepository<JpaChatArchiveContent, Long>
