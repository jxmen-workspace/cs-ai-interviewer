package dev.jxmen.cs.ai.interviewer.adapter.output.persistence

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatArchiveCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatArchiveCommandRepository :
    ChatArchiveCommandRepository,
    JpaRepository<JpaChatArchive, Long>
