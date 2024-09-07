package dev.jxmen.cs.ai.interviewer.application.port.input.dto

import dev.jxmen.cs.ai.interviewer.persistence.entity.chat.JpaChat
import dev.jxmen.cs.ai.interviewer.persistence.entity.member.JpaMember
import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject

data class CreateSubjectAnswerCommand(
    val jpaSubject: JpaSubject,
    val answer: String,
    val jpaMember: JpaMember,
    val jpaChats: List<JpaChat>,
)
