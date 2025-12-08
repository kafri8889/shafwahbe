package com.anafthdev.shafwahbe.util

import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.EmployeeRepository
import com.anafthdev.shafwahbe.service.JwtService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val employeeRepository: EmployeeRepository
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.servletPath

        // Skip auth filter untuk endpoint public (login, register, etc)
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response)
            return
        }

        val authHeader: String? = request.getHeader("Authorization")

        // Kalau nggak ada auth header, biarin
        // Endpoint yang butuh auth bakal ditolak sama spring security config
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val jwt: String = authHeader.substring(7)

        val username: String = try {
            jwtService.extractUsername(jwt)
        } catch (ex: ExpiredJwtException) {
            writeJwtError(response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED")
            return
        } catch (ex: JwtException) {
            writeJwtError(response, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID")
            return
        }

        if (SecurityContextHolder.getContext().authentication == null) {
            val employee = employeeRepository.findByUsername(username)

            if (employee != null && jwtService.isTokenValid(jwt, employee)) {
                val authorities = mutableListOf<SimpleGrantedAuthority>().apply {
                    add(SimpleGrantedAuthority("ROLE_${employee.role.name}"))
                    add(SimpleGrantedAuthority("ACCESS_${employee.accessRole.name}"))
                }

                val authToken = UsernamePasswordAuthenticationToken(
                    employee,
                    null,
                    authorities
                )

                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun writeJwtError(
        response: HttpServletResponse,
        status: HttpStatus,
        message: String
    ) {
        response.status = status.value()
        response.contentType = "application/json"

        val body = ApiResponse<Unit>(
            success = false,
            message = message,
            data = null
        )

        val mapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        response.writer.write(mapper.writeValueAsString(body))
    }
}
