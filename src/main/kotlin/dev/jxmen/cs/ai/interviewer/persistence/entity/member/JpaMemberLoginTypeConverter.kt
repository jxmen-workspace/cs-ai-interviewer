package dev.jxmen.cs.ai.interviewer.persistence.entity.member

import dev.jxmen.cs.ai.interviewer.domain.member.MemberLoginType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class JpaMemberLoginTypeConverter : AttributeConverter<MemberLoginType, String> {
    override fun convertToDatabaseColumn(attribute: MemberLoginType?): String = attribute.toString().lowercase()

    override fun convertToEntityAttribute(dbData: String?): MemberLoginType = MemberLoginType.valueOf(dbData!!.uppercase())
}
