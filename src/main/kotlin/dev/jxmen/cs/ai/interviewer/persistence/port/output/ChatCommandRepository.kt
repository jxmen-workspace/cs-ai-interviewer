package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import org.springframework.data.jpa.repository.JpaRepository

interface ChatCommandRepository : JpaRepository<JpaChat, Long> {
    fun save(jpaChat: JpaChat): JpaChat

    override fun deleteAllInBatch(chats: Iterable<JpaChat>)
}
