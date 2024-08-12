package dev.jxmen.cs.ai.interviewer.global

import dev.jxmen.cs.ai.interviewer.domain.chat.exceptions.AllAnswersUsedException
import dev.jxmen.cs.ai.interviewer.domain.subject.exceptions.SubjectNotFoundException
import dev.jxmen.cs.ai.interviewer.global.dto.ApiResponse
import dev.jxmen.cs.ai.interviewer.global.dto.ErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalControllerAdvice {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.badRequest().body(
            ErrorResponse(400, e.message ?: "Bad Request"),
        )

    @ExceptionHandler(SubjectNotFoundException::class)
    fun handleSubjectNotFoundException(e: SubjectNotFoundException): ResponseEntity<ErrorResponse> = ResponseEntity.notFound().build()

    @ExceptionHandler(AllAnswersUsedException::class)
    fun handleAllAnswersUsedException(e: AllAnswersUsedException): ResponseEntity<ApiResponse<Nothing>> =
        ResponseEntity.badRequest().body(
            ApiResponse.failure(
                code = "ALL_ANSWERS_USED",
                status = 400,
                message = e.message ?: "All answers used",
            ),
        )
}
