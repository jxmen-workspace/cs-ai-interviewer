package dev.jxmen.cs.ai.interviewer.domain.subject

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class SubjectCategoryConverter : AttributeConverter<SubjectCategory, String> {
    /**
     * SubjectCategory -> String
     */
    override fun convertToDatabaseColumn(attribute: SubjectCategory?): String? = attribute?.name?.uppercase()

    /**
     * String -> SubjectCategory
     */
    override fun convertToEntityAttribute(dbData: String?): SubjectCategory? = if (dbData == null) null else SubjectCategory.valueOf(dbData)
}