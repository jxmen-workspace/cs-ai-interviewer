package dev.jxmen.cs.ai.interviewer.infrastructure

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatCommandRepository :
    ChatCommandRepository,
    JpaRepository<Chat, Long>
