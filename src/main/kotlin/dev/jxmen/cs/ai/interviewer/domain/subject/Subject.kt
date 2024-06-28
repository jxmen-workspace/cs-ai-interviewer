package dev.jxmen.cs.ai.interviewer.domain.subject

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment

/**
 * 주제
 */
@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["title", "question"]),
    ],
)
class Subject(
    @Comment("제목")
    @Column(nullable = false)
    val title: String,
    @Comment("질문")
    @Column(nullable = false)
    val question: String,
    @Enumerated(value = EnumType.STRING)
    @Convert(converter = SubjectCategoryConverter::class)
    @Comment("카테고리")
    val category: SubjectCategory,
) : BaseEntity()
