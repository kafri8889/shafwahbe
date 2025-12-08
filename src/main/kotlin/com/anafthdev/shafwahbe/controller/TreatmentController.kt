package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.Treatment
import com.anafthdev.shafwahbe.model.body.BatchTreatmentRequest
import com.anafthdev.shafwahbe.model.body.TreatmentRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.service.TreatmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/treatments")
class TreatmentController(
    private val treatmentService: TreatmentService
) {

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse<List<Treatment>>> = treatmentService.getAll()

    @GetMapping("/active")
    fun getActiveTreatment(): ResponseEntity<ApiResponse<List<Treatment>>> = treatmentService.getActiveTreatment()

    @GetMapping("/inactive")
    fun getInactiveTreatment(): ResponseEntity<ApiResponse<List<Treatment>>> = treatmentService.getInactiveTreatment()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<Treatment>> = treatmentService.getById(id)

    @PostMapping
    fun create(@RequestBody request: TreatmentRequest): ResponseEntity<ApiResponse<Treatment>> {
        return treatmentService.create(request)
    }

    @PostMapping("/batch")
    fun batchCreate(@RequestBody request: BatchTreatmentRequest): ResponseEntity<ApiResponse<List<Treatment>>> {
        return treatmentService.batchCreate(request)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TreatmentRequest): ResponseEntity<ApiResponse<Treatment>> {
        return treatmentService.update(id, request)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = treatmentService.delete(id)
}
