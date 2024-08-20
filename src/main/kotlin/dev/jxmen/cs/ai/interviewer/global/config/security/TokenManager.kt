package dev.jxmen.cs.ai.interviewer.global.config.security

import dev.jxmen.cs.ai.interviewer.global.enum.ErrorType
import dev.jxmen.cs.ai.interviewer.global.exceptions.UnAuthorizedException
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class TokenManager(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
) {
    companion object {
        const val ACCESS_TOKEN_VALIDITY = 1000L * 60L * 10L // 10 minutes
        const val REFRESH_TOKEN_VALIDITY = 1000L * 60L * 60L * 24L * 30L * 3L // 3 months
    }

    private val key =
        run {
            val keyBytes = Decoders.BASE64.decode(jwtSecret)
            Keys.hmacShaKeyFor(keyBytes)
        }

    fun generateTokens(
        id: Long,
        email: String,
        now: Date = Date(),
    ): Token {
        val builder = builder(id = id, email = email, issuedAt = now)

        val accessToken = builder.expiration(Date(now.time + ACCESS_TOKEN_VALIDITY)).compact()
        val refreshToken = builder.expiration(Date(now.time + REFRESH_TOKEN_VALIDITY)).compact()

        return Token(accessToken, refreshToken)
    }

    fun renewAccessToken(
        refreshToken: String,
        now: Date = Date(),
    ): String {
        val claims =
            try {
                Jwts
                    .parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .payload
            } catch (e: Exception) {
                throw UnAuthorizedException(ErrorType.INVALID_TOKEN)
            }

        val memberId = claims.subject
        val builder = builder(id = memberId, email = claims["email"].toString(), issuedAt = now)

        return builder
            .expiration(Date(now.time + ACCESS_TOKEN_VALIDITY))
            .compact()
    }

    fun parseToken(token: String): ParsedToken {
        val parseSignedClaims =
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)

        return ParsedToken(
            memberId = parseSignedClaims.payload.subject.toLong(),
            email = parseSignedClaims.payload["email"].toString(),
        )
    }

    private fun builder(
        id: Long,
        email: String,
        issuedAt: Date,
    ): JwtBuilder = builder(id.toString(), email, issuedAt)

    private fun builder(
        id: String,
        email: String,
        issuedAt: Date,
    ): JwtBuilder =
        Jwts
            .builder()
            .subject(id)
            .claim("email", email)
            .issuedAt(issuedAt)
            .signWith(key)
}

data class Token(
    val accessToken: String,
    val refreshToken: String,
)

data class ParsedToken(
    val memberId: Long,
    val email: String,
)
