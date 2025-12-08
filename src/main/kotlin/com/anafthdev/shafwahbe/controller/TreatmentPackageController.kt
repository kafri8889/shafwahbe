package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.TreatmentPackage
import com.anafthdev.shafwahbe.model.body.BatchTreatmentPackageRequest
import com.anafthdev.shafwahbe.model.body.TreatmentPackageRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.service.TreatmentPackageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/treatment-packages")
class TreatmentPackageController(
    private val treatmentPackageService: TreatmentPackageService
) {

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse<List<TreatmentPackage>>> {
        return treatmentPackageService.getAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<TreatmentPackage>> {
        return treatmentPackageService.getById(id)
    }

    @PostMapping
    fun create(@RequestBody request: TreatmentPackageRequest): ResponseEntity<ApiResponse<TreatmentPackage>> {
        return treatmentPackageService.create(request)
    }

    @PostMapping("/batch")
    fun batchCreate(@RequestBody request: BatchTreatmentPackageRequest): ResponseEntity<ApiResponse<List<TreatmentPackage>>> {
        return treatmentPackageService.batchCreate(request)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: TreatmentPackageRequest
    ): ResponseEntity<ApiResponse<TreatmentPackage>> {
        return treatmentPackageService.update(id, request)
    }


    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<String>> {
        return treatmentPackageService.delete(id)
    }
}
