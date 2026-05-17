package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.Staff
import com.anafthdev.shafwahbe.model.body.StaffRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.StaffRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class StaffService(
    private val staffRepository: StaffRepository
) {

    fun getAllStaff(): ResponseEntity<ApiResponse<List<Staff>>> {
        val staff = staffRepository.findAll().sortedBy { it.name.lowercase() }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${staff.size} staff.", data = staff))
    }

    fun getActiveStaff(): ResponseEntity<ApiResponse<List<Staff>>> {
        val staff = staffRepository.findAllByActiveTrueOrderByNameAsc()
        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${staff.size} active staff.", data = staff))
    }

    fun getStaffById(id: Long): ResponseEntity<ApiResponse<Staff>> {
        val staff = staffRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Staff with ID $id not found."))

        return ResponseEntity.ok(ApiResponse(success = true, message = "Staff with ID $id found.", data = staff))
    }

    fun createStaff(request: StaffRequest): ResponseEntity<ApiResponse<Staff>> {
        val payload = request.toStaff()
        if (payload.name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Staff name is required."))
        }

        val saved = staffRepository.save(payload)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Staff '${saved.name}' created successfully.", data = saved))
    }

    fun updateStaff(id: Long, request: StaffRequest): ResponseEntity<ApiResponse<Staff>> {
        val existing = staffRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Staff with ID $id not found."))

        if (request.name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Staff name is required."))
        }

        val saved = staffRepository.save(
            existing.copy(
                name = request.name.trim(),
                role = request.role.trim().ifBlank { "Stylist" },
                phoneNumber = request.phoneNumber.trim(),
                active = request.active
            )
        )

        return ResponseEntity.ok(ApiResponse(success = true, message = "Staff '${saved.name}' updated successfully.", data = saved))
    }

    fun deactivateStaff(id: Long): ResponseEntity<ApiResponse<Unit>> {
        val existing = staffRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Staff with ID $id not found."))

        staffRepository.save(existing.copy(active = false))
        return ResponseEntity.ok(ApiResponse(success = true, message = "Staff with ID $id deactivated successfully."))
    }

    private fun StaffRequest.toStaff(): Staff = Staff(
        name = name.trim(),
        role = role.trim().ifBlank { "Stylist" },
        phoneNumber = phoneNumber.trim(),
        active = active
    )
}
