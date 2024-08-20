package dev.jxmen.cs.ai.interviewer.global.config.security

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
        val accessToken =
            Jwts
                .builder()
                .subject(id.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(Date(now.time + ACCESS_TOKEN_VALIDITY))
                .signWith(key)
                .compact()

        val refreshToken =
            Jwts
                .builder()
                .subject(id.toString())
                .issuedAt(now)
                .expiration(Date(now.time + REFRESH_TOKEN_VALIDITY))
                .signWith(key)
                .compact()

        return Token(accessToken, refreshToken)
    }

    fun refreshAccessToken(
        refreshToken: String,
        now: Date = Date(),
    ): String {
        val claims =
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken)
                .payload

        val userId = claims.subject.toLong()

        return Jwts
            .builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + ACCESS_TOKEN_VALIDITY))
            .signWith(key)
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
}

data class Token(
    val accessToken: String,
    val refreshToken: String,
)

data class ParsedToken(
    val memberId: Long,
    val email: String,
)
