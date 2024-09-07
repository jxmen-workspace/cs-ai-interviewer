package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat

interface ChatCommandRepository {
    fun save(jpaChat: JpaChat): JpaChat

    fun deleteAllInBatch(chats: Iterable<JpaChat>)
}
