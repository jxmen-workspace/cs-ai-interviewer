package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChatArchive
import org.springframework.data.jpa.repository.JpaRepository

interface ChatArchiveCommandRepository : JpaRepository<JpaChatArchive, Long>
