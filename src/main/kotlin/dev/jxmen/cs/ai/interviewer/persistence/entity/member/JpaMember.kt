package dev.jxmen.cs.ai.interviewer.persistence.entity.member

import dev.jxmen.cs.ai.interviewer.domain.member.MemberLoginType
import dev.jxmen.cs.ai.interviewer.persistence.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment
import java.io.Serializable
import java.time.LocalDateTime

@Suppress("ktlint:standard:no-blank-line-in-list")
@Entity
@Table(
    name = "member",
    // TODO: 구글 외 다른 소셜 로그인을 지원한다면 email unique 제약조건을 제거할지 검토 필요
    uniqueConstraints = [UniqueConstraint(columnNames = ["email"])],
)
class JpaMember(

    @Column(nullable = false)
    @Comment("이름")
    val name: String,

    // TODO: 구글 외 다른 소셜 로그인을 지원한다면 email unique 제약조건을 제거할지 검토 필요
    @Column(nullable = false, unique = true)
    @Comment("이메일")
    val email: String,

    @Column(nullable = false)
    @Convert(converter = JpaMemberLoginTypeConverter::class)
    @Comment("로그인 타입")
    val loginType: MemberLoginType,

) : BaseEntity(),
    Serializable {
    companion object {
        fun createGoogleMember(
            name: String,
            email: String,
        ): JpaMember =
            JpaMember(
                name = name,
                email = email,
                loginType = MemberLoginType.GOOGLE,
            )

        fun createWithId(
            id: Long,
            name: String,
            email: String,
            loginType: MemberLoginType,
        ): JpaMember {
            val jpaMember = JpaMember(name, email, loginType)
            jpaMember.id = id
            return jpaMember
        }
    }

    constructor(id: Long, name: String, email: String, loginType: MemberLoginType) : this(
        name = name,
        email = email,
        loginType = loginType,
    ) {
        super.id = id
    }

    constructor(
        id: Long,
        name: String,
        email: String,
        loginType: MemberLoginType,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) : this(
        name = name,
        email = email,
        loginType = loginType,

    ) {
        super.id = id
        super.createdAt = createdAt
        super.updatedAt = updatedAt
    }

    fun equalsId(other: JpaMember): Boolean = id == other.id
}
