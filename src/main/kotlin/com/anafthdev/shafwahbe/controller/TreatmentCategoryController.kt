package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.TreatmentCategory
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.service.TreatmentCategoryService
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
@RequestMapping("/api/treatment-category")
class TreatmentCategoryController(
    private val treatmentCategoryService: TreatmentCategoryService
) {

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse<List<TreatmentCategory>>> {
        return treatmentCategoryService.getAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<TreatmentCategory>> {
        return treatmentCategoryService.getById(id)
    }

    @PostMapping
    fun create(@RequestBody category: TreatmentCategory): ResponseEntity<ApiResponse<TreatmentCategory>> {
        return treatmentCategoryService.create(category)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody category: TreatmentCategory): ResponseEntity<ApiResponse<TreatmentCategory>> {
        return treatmentCategoryService.update(id, category)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<ApiResponse<String>> {
        return treatmentCategoryService.delete(id)
    }
}
