package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.Employee
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(

    @Value("\${jwt.secret}")
    private val secret: String,

    @Value("\${jwt.expiration-ms}")
    private val expirationMs: Long
) {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    fun generateToken(employee: Employee): String {
        val now = System.currentTimeMillis()

        return Jwts.builder()
            .setSubject(employee.username)
            .claim("role", employee.role.name)
            .claim("accessRole", employee.accessRole.name)
            .setIssuedAt(Date(now))
            .setExpiration(Date(now + expirationMs))
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
            .subject

    fun isTokenExpired(token: String): Boolean {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body

        return claims.expiration.before(Date())
    }

    fun isTokenValid(token: String, employee: Employee): Boolean {
        val username = extractUsername(token)
        return username == employee.username && !isTokenExpired(token)
    }
}
