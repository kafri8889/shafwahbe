package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.enums.PriceType
import com.anafthdev.shafwahbe.model.Treatment
import com.anafthdev.shafwahbe.model.body.BatchTreatmentRequest
import com.anafthdev.shafwahbe.model.body.TreatmentRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.TreatmentCategoryRepository
import com.anafthdev.shafwahbe.repository.TreatmentRepository
import org.springframework.cloud.client.loadbalancer.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TreatmentService(
    private val treatmentRepository: TreatmentRepository,
    private val treatmentCategoryRepository: TreatmentCategoryRepository
) {

    fun getAll(): ResponseEntity<ApiResponse<List<Treatment>>> {
        val treatments = treatmentRepository.findAll()
        return ResponseEntity
            .ok(ApiResponse(
                success = true,
                message = "Found ${treatments.size} treatments.",
                data = treatments
            ))
    }

    fun getActiveTreatment(): ResponseEntity<ApiResponse<List<Treatment>>> {
        val treatments = treatmentRepository.findAllByActive(true)
        return ResponseEntity
            .ok(ApiResponse(
                success = true,
                message = "Found ${treatments.size} active treatments.",
                data = treatments
            ))
    }

    fun getInactiveTreatment(): ResponseEntity<ApiResponse<List<Treatment>>> {
        val treatments = treatmentRepository.findAllByActive(false)
        return ResponseEntity
            .ok(ApiResponse(
                success = true,
                message = "Found ${treatments.size} inactive treatments.",
                data = treatments
            ))
    }

    fun getById(id: Long): ResponseEntity<ApiResponse<Treatment>> {
        val treatment = treatmentRepository.findById(id)

        if (treatment.isPresent) {
            return ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Treatment found.",
                data = treatment.get()
            ))
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(
                success = false,
                message = "Treatment with id $id not found!",
            ))
    }

    @Transactional
    fun create(request: TreatmentRequest): ResponseEntity<ApiResponse<Treatment>> {
        val category = treatmentCategoryRepository.findById(request.categoryId)

        if (category.isEmpty) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Category with id ${request.categoryId} does not exist!",
                ))
        }

        if (request.priceType != PriceType.Fixed && request.prices.size == 1) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(
                    success = false,
                    message = "Price type is \"${request.priceType}\", but the price contain only ONE element!. Add new price element or change price type to \"${PriceType.Fixed}\"",
                ))
        }

        val treatment = Treatment(
            category = category.get(),
            title = request.title,
            active = request.active,
            priceType = request.priceType,
            prices = request.prices
        )

        treatmentRepository.save(treatment)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "Treatment created!",
                data = treatment
            ))
    }

    fun batchCreate(request: BatchTreatmentRequest): ResponseEntity<ApiResponse<List<Treatment>>> {
        val categories = treatmentCategoryRepository.findAllById(
            request.treatments.map { it.categoryId }.distinct()
        ).associateBy { it.id }

        val treatments = request.treatments.map {
            val category = categories[it.categoryId]

            if (category == null) return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Category with id ${it.categoryId} not found!",
                    data = emptyList()
                ))

            Treatment(
                category = category,
                title = it.title,
                active = it.active,
                priceType = it.priceType,
                prices = it.prices,
            )
        }

        treatmentRepository.saveAll(treatments)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "${treatments.size} Treatments created!",
                data = treatments
            ))
    }

    @Transactional
    fun update(id: Long, request: TreatmentRequest): ResponseEntity<ApiResponse<Treatment>> {
        val treatment = getById(id)
        val newCategory = treatmentCategoryRepository.findById(request.categoryId)

        if (treatment.statusCode == HttpStatus.NOT_FOUND) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Treatment with id $id does not exist!",
                ))
        }

        if (newCategory.isEmpty) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Category with id ${request.categoryId} does not exist!",
                ))
        }

        val updated = treatment.body!!.data!!.copy(
            title = request.title,
            prices = request.prices,
            active = request.active,
            priceType = request.priceType,
            category = newCategory.get()
        )

        treatmentRepository.save(updated)

        return ResponseEntity
            .ok(ApiResponse(
                success = true,
                message = "Treatment updated!",
                data = updated
            ))
    }


    @Transactional
    fun delete(id: Long): ResponseEntity<ApiResponse<String>> {
        if (!treatmentRepository.existsById(id)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Treatment with ID $id not found."
                ))
        }

        treatmentRepository.deleteById(id)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse(
                success = true,
                message = "Treatment with ID $id deleted.",
            ))
    }
}
