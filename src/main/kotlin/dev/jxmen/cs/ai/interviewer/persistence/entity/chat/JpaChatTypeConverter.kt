package dev.jxmen.cs.ai.interviewer.persistence.entity.chat

import dev.jxmen.cs.ai.interviewer.domain.chat.ChatType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class JpaChatTypeConverter : AttributeConverter<ChatType, String> {
    override fun convertToDatabaseColumn(attribute: ChatType): String = attribute.name.lowercase()

    override fun convertToEntityAttribute(dbData: String): ChatType = ChatType.valueOf(dbData.uppercase())
}
