package dev.jxmen.cs.ai.interviewer.domain.subject

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import jakarta.persistence.Entity
import org.hibernate.annotations.Comment

@Entity
class Subject : BaseEntity() {
    @Comment("제목")
    lateinit var title: String

    @Comment("질문")
    lateinit var question: String
}
