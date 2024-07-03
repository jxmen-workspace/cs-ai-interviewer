package dev.jxmen.cs.ai.interviewer.domain.subject

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class SubjectCategoryConverter : AttributeConverter<SubjectCategory, String> {
    /**
     * SubjectCategory -> String
     */
    override fun convertToDatabaseColumn(attribute: SubjectCategory): String = attribute.name.lowercase()

    /**
     * String -> SubjectCategory
     */
    override fun convertToEntityAttribute(dbData: String): SubjectCategory = SubjectCategory.valueOf(dbData.uppercase())
}
