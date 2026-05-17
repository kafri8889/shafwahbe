package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.Staff
import com.anafthdev.shafwahbe.model.body.StaffRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.service.StaffService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/staff")
class StaffController(
    private val staffService: StaffService
) {

    @GetMapping
    fun getAllStaff(): ResponseEntity<ApiResponse<List<Staff>>> =
        staffService.getAllStaff()

    @GetMapping("/active")
    fun getActiveStaff(): ResponseEntity<ApiResponse<List<Staff>>> =
        staffService.getActiveStaff()

    @GetMapping("/{id}")
    fun getStaffById(@PathVariable id: Long): ResponseEntity<ApiResponse<Staff>> =
        staffService.getStaffById(id)

    @PostMapping
    fun createStaff(@RequestBody request: StaffRequest): ResponseEntity<ApiResponse<Staff>> =
        staffService.createStaff(request)

    @PutMapping("/{id}")
    fun updateStaff(@PathVariable id: Long, @RequestBody request: StaffRequest): ResponseEntity<ApiResponse<Staff>> =
        staffService.updateStaff(id, request)

    @DeleteMapping("/{id}")
    fun deactivateStaff(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> =
        staffService.deactivateStaff(id)
}
