package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.Employee
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.EmployeeResponse
import com.anafthdev.shafwahbe.service.EmployeeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/employees")
class EmployeeController(
    private val employeeService: EmployeeService
) {

    @GetMapping
    fun getAllEmployees(): ResponseEntity<ApiResponse<List<EmployeeResponse>>> =
        employeeService.getAllEmployees()

    @GetMapping("/{id}")
    fun getEmployeeById(@PathVariable id: Long): ResponseEntity<ApiResponse<EmployeeResponse>> =
        employeeService.getEmployeeById(id)

    @PutMapping("/{id}")
    fun updateEmployee(@PathVariable id: Long, @RequestBody employeeDetails: Employee): ResponseEntity<ApiResponse<EmployeeResponse>> =
        employeeService.updateEmployee(id, employeeDetails)

    @DeleteMapping("/{id}")
    fun deleteEmployee(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> =
        employeeService.deleteEmployee(id)
}