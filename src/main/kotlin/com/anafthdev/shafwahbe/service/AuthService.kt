package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.body.LoginRequest
import com.anafthdev.shafwahbe.model.body.RegisterRequest
import com.anafthdev.shafwahbe.model.Employee
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.EmployeeResponse
import com.anafthdev.shafwahbe.model.response.LoginResponse
import com.anafthdev.shafwahbe.repository.EmployeeRepository
import com.anafthdev.shafwahbe.util.toEmployeeResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val employeeRepository: EmployeeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Value("\${superadmin.password}")
    private lateinit var superadminPassword: String

    fun register(
        request: RegisterRequest,
        superAdminPassword: String?,
    ): ResponseEntity<ApiResponse<EmployeeResponse>> {

        if (superAdminPassword == null) return ResponseEntity.badRequest().body(ApiResponse(
            success = false,
            message = "Masukkan super admin password!",
        ))

        if (superAdminPassword != superadminPassword) return ResponseEntity.badRequest().body(ApiResponse(
            success = false,
            message = "Super admin password salah!",
        ))

        val isExists = employeeRepository.existsByUsername(request.username)

        if (isExists) return ResponseEntity.badRequest().body(ApiResponse(
            success = false,
            message = "User with username ${request.username} exists!"
        ))

        val employee = Employee(
            name = request.name,
            username = request.username,
            password = passwordEncoder.encode(request.password),
            phoneNumber = request.phoneNumber,
            role = request.role,
            accessRole = request.accessRole,
        )

        employeeRepository.save(employee)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "Successfully registered",
                data = employee.toEmployeeResponse()
            ))
    }

    fun login(
        request: LoginRequest
    ): ResponseEntity<ApiResponse<String>> {
        val employee = employeeRepository.findByUsername(request.username)
            ?: return ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Username ${request.username} does not exist!",
            ))

        if (!passwordEncoder.matches(request.password, employee.password)) {
            return ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Password does not match!"
            ))
        }

        return ResponseEntity
            .ok(ApiResponse(
                success = true,
                message = "Successfully logged in!",
                data = jwtService.generateToken(employee)
            ))
    }
}