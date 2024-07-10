package dev.jxmen.cs.ai.interviewer

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class VersionApi(
    @Value("\${version}")
    private val version: String,
) {
    @GetMapping("/api/version")
    fun getVersion(): VersionResponse = VersionResponse(version)
}

data class VersionResponse(
    val version: String,
)
