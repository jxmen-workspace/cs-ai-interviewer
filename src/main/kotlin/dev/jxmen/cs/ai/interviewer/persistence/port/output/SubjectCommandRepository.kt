package dev.jxmen.cs.ai.interviewer.persistence.port.output

import dev.jxmen.cs.ai.interviewer.persistence.entity.subject.JpaSubject
import org.springframework.data.jpa.repository.JpaRepository

// NOTE: 별도 구현체 사용 시 타입을 찾을 수 없는 이슈가 있어 JpaRepository 바로 상속하도록 구현
interface SubjectCommandRepository : JpaRepository<JpaSubject, Long>
