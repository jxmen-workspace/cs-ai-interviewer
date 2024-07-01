package dev.jxmen.cs.ai.interviewer.domain.chat

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class ChatTypeConverter : AttributeConverter<ChatType, String> {
    override fun convertToDatabaseColumn(attribute: ChatType): String = attribute.name

    override fun convertToEntityAttribute(dbData: String): ChatType = ChatType.valueOf(dbData)
}
