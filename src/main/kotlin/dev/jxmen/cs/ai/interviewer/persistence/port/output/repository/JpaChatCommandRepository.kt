package dev.jxmen.cs.ai.interviewer.persistence.port.output.repository

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.port.output.ChatCommandRepository
import org.springframework.data.jpa.repository.JpaRepository

interface JpaChatCommandRepository :
    ChatCommandRepository,
    JpaRepository<JpaChat, Long>
