package com.anafthdev.shafwahbe.config

import com.anafthdev.shafwahbe.util.JwtAuthFilter
import corsConfigurationSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider // Diperlukan jika ada custom auth provider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
    // private val authenticationProvider: AuthenticationProvider
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // Endpoint publik (login, register, dll.)
                    .requestMatchers("/api/auth/**").permitAll()

                    // Endpoint yang perlu autentikasi
                    .requestMatchers("/api/employees/**").authenticated()

                    // Aturan untuk role spesifik jika diperlukan:
                    // .requestMatchers(HttpMethod.POST, "/api/employees").hasRole("ADMIN") // Contoh, hanya ADMIN bisa POST
                    // .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAuthority("ACCESS_SUPERADMIN") // Contoh pakai authority lain

                    // Semua request lain harus diautentikasi
                    .anyRequest().authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}