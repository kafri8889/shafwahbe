package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.Employee
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.EmployeeResponse
import com.anafthdev.shafwahbe.repository.EmployeeRepository
import com.anafthdev.shafwahbe.util.toEmployeeResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.Optional // Pastikan import ini ada

@Service
class EmployeeService(
    private val employeeRepository: EmployeeRepository,
     private val passwordEncoder: PasswordEncoder
) {

    fun getAllEmployees(): ResponseEntity<ApiResponse<List<EmployeeResponse>>> {
        val employees = employeeRepository.findAll()
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${employees.size} employees.",
            data = employees.map { it.toEmployeeResponse() }
        ))
    }

    fun getEmployeeById(id: Long): ResponseEntity<ApiResponse<EmployeeResponse>> {
        val employeeOptional = employeeRepository.findById(id)
        return if (employeeOptional.isPresent) {
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Employee with ID $id found.",
                data = employeeOptional.get().toEmployeeResponse()
            ))
        } else {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Employee with ID $id not found."
                ))
        }
    }

    fun updateEmployee(id: Long, employeeDetails: Employee): ResponseEntity<ApiResponse<EmployeeResponse>> {
        val existingEmployeeOptional = employeeRepository.findById(id)

        if (existingEmployeeOptional.isEmpty) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Employee with ID $id not found."
                ))
        }

        val existingEmployee = existingEmployeeOptional.get()

        // Validasi keunikan username jika username diubah
        if (existingEmployee.username != employeeDetails.username && employeeRepository.findByUsername(employeeDetails.username) != null) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse(
                    success = false,
                    message = "New username '${employeeDetails.username}' already exists."
                ))
        }

        var passwordToUpdate = existingEmployee.password
        if (employeeDetails.password.isNotBlank() && employeeDetails.password != existingEmployee.password) {
             passwordToUpdate = passwordEncoder.encode(employeeDetails.password)
        }


        val updatedEmployee = existingEmployee.copy(
            name = employeeDetails.name,
            username = employeeDetails.username,
            password = passwordToUpdate,
            role = employeeDetails.role,
            accessRole = employeeDetails.accessRole,
            phoneNumber = employeeDetails.phoneNumber
        )

        val savedEmployee = employeeRepository.save(updatedEmployee)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Employee '${savedEmployee.name}' updated successfully.",
            data = savedEmployee.toEmployeeResponse()
        ))
    }

    fun deleteEmployee(id: Long): ResponseEntity<ApiResponse<Unit>> {
        if (!employeeRepository.existsById(id)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Employee with ID $id not found."
                ))
        }

        employeeRepository.deleteById(id)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Employee with ID $id deleted successfully."
        ))
    }
}