package dev.jxmen.cs.ai.interviewer.domain.member

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class MemberLoginTypeConverter : AttributeConverter<MemberLoginType, String> {
    override fun convertToDatabaseColumn(attribute: MemberLoginType?): String {
        return attribute.toString().lowercase()
    }

    override fun convertToEntityAttribute(dbData: String?): MemberLoginType {
        return MemberLoginType.valueOf(dbData!!.uppercase())
    }
}
