package dev.jxmen.cs.ai.interviewer.domain.subject

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import jakarta.persistence.Entity
import org.hibernate.annotations.Comment

@Entity
class Subject(
    @Comment("제목")
    val title: String,
    @Comment("질문")
    val question: String,
) : BaseEntity()
