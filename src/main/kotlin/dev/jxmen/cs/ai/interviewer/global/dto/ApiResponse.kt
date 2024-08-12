package dev.jxmen.cs.ai.interviewer.global.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiErrorResponse? = null,
) {
    companion object {
        fun failure(
            code: String,
            status: Int,
            message: String,
        ): ApiResponse<Nothing> =
            ApiResponse(
                success = false,
                data = null,
                error =
                    ApiErrorResponse(
                        code = code,
                        status = status,
                        message = message,
                    ),
            )
    }
}

data class ApiErrorResponse(
    val code: String,
    val status: Int,
    val message: String,
)
