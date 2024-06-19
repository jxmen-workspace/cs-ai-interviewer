package dev.jxmen.cs.ai.interviewer.domain.subject

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.hibernate.annotations.Comment

@Entity
class Subject(
    @Comment("제목")
    val title: String,
    @Comment("질문")
    val question: String,
    @Enumerated(value = EnumType.STRING)
    @Comment("카테고리")
    val category: SubjectCategory,
) : BaseEntity()
