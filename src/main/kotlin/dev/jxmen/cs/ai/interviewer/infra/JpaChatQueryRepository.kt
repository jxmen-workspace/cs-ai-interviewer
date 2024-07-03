package dev.jxmen.cs.ai.interviewer.infra

import dev.jxmen.cs.ai.interviewer.domain.chat.Chat
import dev.jxmen.cs.ai.interviewer.domain.chat.ChatQueryRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatQueryRepository :
    ChatQueryRepository,
    JpaRepository<Chat, Long>
