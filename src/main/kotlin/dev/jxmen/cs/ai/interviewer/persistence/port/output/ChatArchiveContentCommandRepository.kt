package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchiveContent
import org.springframework.data.jpa.repository.JpaRepository

interface ChatArchiveContentCommandRepository : JpaRepository<JpaChatArchiveContent, Long>
