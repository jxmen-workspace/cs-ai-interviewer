package dev.jxmen.cs.ai.interviewer.domain.member

import dev.jxmen.cs.ai.interviewer.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Comment
import java.io.Serializable

@Suppress("ktlint:standard:no-blank-line-in-list")
@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["email"])],
)
class Member(

    @Column(nullable = false)
    @Comment("이름")
    val name: String,

    @Column(nullable = false, unique = true)
    @Comment("이메일")
    val email: String,

    @Column(nullable = false)
    @Convert(converter = MemberLoginTypeConverter::class)
    @Comment("로그인 타입")
    val loginType: MemberLoginType,

) : BaseEntity(),
    Serializable {
    companion object {
        fun createGoogleMember(
            name: String,
            email: String,
        ): Member =
            Member(
                name = name,
                email = email,
                loginType = MemberLoginType.GOOGLE,
            )

        fun createWithId(
            id: Long,
            name: String,
            email: String,
            loginType: MemberLoginType,
        ): Member {
            val member = Member(name, email, loginType)
            member.id = id
            return member
        }
    }
}
