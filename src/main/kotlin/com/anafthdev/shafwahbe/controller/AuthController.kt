package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.Employee
import com.anafthdev.shafwahbe.model.body.LoginRequest
import com.anafthdev.shafwahbe.model.body.RegisterRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.EmployeeResponse
import com.anafthdev.shafwahbe.model.response.LoginResponse
import com.anafthdev.shafwahbe.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest,
        @RequestParam("superAdminPassword") superAdminPassword: String?,
    ): ResponseEntity<ApiResponse<EmployeeResponse>> {
        return authService.register(request, superAdminPassword)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<String>> {
        return authService.login(request)
    }
}