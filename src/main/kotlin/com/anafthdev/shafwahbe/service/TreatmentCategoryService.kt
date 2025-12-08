package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.TreatmentCategory
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.TreatmentCategoryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class TreatmentCategoryService(
    private val treatmentCategoryRepository: TreatmentCategoryRepository
) {

    fun getAll(): ResponseEntity<ApiResponse<List<TreatmentCategory>>> {
        val categories = treatmentCategoryRepository.findAll()

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${categories.size} categories",
            data = categories
        ))
    }

    fun getById(id: Long): ResponseEntity<ApiResponse<TreatmentCategory>> {
        val category = treatmentCategoryRepository.findById(id)

        if (category.isPresent) {
            return ResponseEntity
                .status(HttpStatus.FOUND)
                .body(ApiResponse(
                    success = true,
                    message = "Category found.",
                    data = category.get()
                ))
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(
                success = false,
                message = "Category not found.",
            ))
    }

    fun create(category: TreatmentCategory): ResponseEntity<ApiResponse<TreatmentCategory>> {
        val category = treatmentCategoryRepository.save(category)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "Created ${category.id} (${category.title})",
                data = category
            ))
    }

    fun update(id: Long, category: TreatmentCategory): ResponseEntity<ApiResponse<TreatmentCategory>> {
        val existing = getById(id)

        if (existing.statusCode == HttpStatus.FOUND) {
            val updated = existing.body!!.data!!.copy(
                title = category.title,
                subTitle = category.subTitle,
                notes = category.notes
            )

            val updatedCategory = treatmentCategoryRepository.save(updated)

            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse(
                    success = true,
                    message = "Updated ${updatedCategory.id} (${updatedCategory.title})",
                    data = updatedCategory
                ))
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(
                success = false,
                message = "Failed to update, category with ID $id not found!",
            ))
    }

    fun delete(id: Long): ResponseEntity<ApiResponse<String>> {
        if (!treatmentCategoryRepository.existsById(id)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Employee with ID $id not found."
                ))
        }

        treatmentCategoryRepository.deleteById(id)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse(
                success = true,
                message = "Category with ID $id deleted.",
            ))
    }
}
