package dev.jxmen.cs.ai.interviewer.domain.subject

enum class SubjectCategory(
    private val s: String,
) {
    DSA("dsa"),
    NETWORK("network"),
    DATABASE("database"),
    OS("os"),
    ;

    companion object {
        fun fromString(s: String): SubjectCategory =
            entries.firstOrNull { it.s == s } ?: throw IllegalArgumentException("No such enum constant $s")
    }
}
