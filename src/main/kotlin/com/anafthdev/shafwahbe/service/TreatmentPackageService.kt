package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.Treatment
import com.anafthdev.shafwahbe.model.TreatmentPackage
import com.anafthdev.shafwahbe.model.body.BatchTreatmentPackageRequest
import com.anafthdev.shafwahbe.model.body.BatchTreatmentRequest
import com.anafthdev.shafwahbe.model.body.TreatmentPackageRequest
import com.anafthdev.shafwahbe.model.body.TreatmentRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.TreatmentCategoryRepository
import com.anafthdev.shafwahbe.repository.TreatmentPackageRepository
import com.anafthdev.shafwahbe.repository.TreatmentRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TreatmentPackageService(
    private val treatmentPackageRepository: TreatmentPackageRepository,
    private val treatmentRepository: TreatmentRepository,
    private val treatmentCategoryRepository: TreatmentCategoryRepository
) {

    fun getAll(): ResponseEntity<ApiResponse<List<TreatmentPackage>>> {
        val packages = treatmentPackageRepository.findAll()
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "${packages.size} Packages Found",
            data = packages,
        ))
    }

    fun getById(id: Long): ResponseEntity<ApiResponse<TreatmentPackage>> {
        val treatmentPackage = treatmentPackageRepository.findById(id)

        if (treatmentPackage.isEmpty) return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse(
                success = false,
                message = "Treatment package not found!",
            ))

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Treatment package found.",
            data = treatmentPackage.get()
        ))
    }

    fun create(request: TreatmentPackageRequest): ResponseEntity<ApiResponse<TreatmentPackage>> {
        val treatments = treatmentRepository.findAllById(request.treatmentIds)
        val category = treatmentCategoryRepository.findById(request.categoryId)

        if (category.isEmpty) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Category with id ${request.categoryId} does not exist!",
                ))
        }

        val newPackage = TreatmentPackage(
            title = request.title,
            category = category.get(),
            price = request.price,
            active = request.active,
            treatments = treatments
        )

        treatmentPackageRepository.save(newPackage)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "${newPackage.title} package created.",
                data = newPackage
            ))
    }

    fun batchCreate(request: BatchTreatmentPackageRequest): ResponseEntity<ApiResponse<List<TreatmentPackage>>> {
        val categories = treatmentCategoryRepository.findAllById(
            request.packages.map { it.categoryId }.distinct()
        ).associateBy { it.id }

        val treatments = treatmentRepository.findAllById(
            request.packages
                .map { it.treatmentIds }
                .flatten()
                .distinct()
        ).associateBy { it.id }

        val packages = request.packages.map {
            val category = categories[it.categoryId]
            val packageTreatments = arrayListOf<Treatment>()

            if (category == null) return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Category with id ${it.categoryId} not found!",
                    data = emptyList()
                ))

            for (treatmentId in it.treatmentIds) {
                val treatment = treatments[treatmentId]

                if (treatment == null) return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse(
                        success = false,
                        message = "Treatment with id $treatmentId not found!",
                        data = emptyList()
                    ))

                packageTreatments.add(treatment)
            }

            TreatmentPackage(
                category = category,
                title = it.title,
                active = it.active,
                price = it.price,
                treatments = packageTreatments
            )
        }

        treatmentPackageRepository.saveAll(packages)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse(
                success = true,
                message = "${packages.size} treatment package created.",
                data = packages
            ))
    }

    @Transactional
    fun update(id: Long, request: TreatmentPackageRequest): ResponseEntity<ApiResponse<TreatmentPackage>> {
        val treatmentPackage = getById(id)
        val newCategory = treatmentCategoryRepository.findById(request.categoryId)
        val treatments = treatmentRepository.findAllById(
            request.treatmentIds.distinct()
        ).associateBy { it.id }

        if (treatmentPackage.statusCode == HttpStatus.NOT_FOUND) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Treatment package with id $id does not exist!",
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

        val newTreatment = arrayListOf<Treatment>()

        for (treatmentId in request.treatmentIds) {
            val treatment = treatments[treatmentId]

            if (treatment == null) return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Treatment with id $treatmentId not found!",
                ))

            newTreatment.add(treatment)
        }

        val updated = treatmentPackage.body!!.data!!.copy(
            title = request.title,
            price = request.price,
            active = request.active,
            category = newCategory.get(),
            treatments = newTreatment
        )

        treatmentPackageRepository.save(updated)

        return ResponseEntity
            .ok(ApiResponse(
                success = true,
                message = "Treatment package updated!",
                data = updated
            ))
    }

    fun delete(id: Long): ResponseEntity<ApiResponse<String>> {
        if (!treatmentPackageRepository.existsById(id)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Treatment package with ID $id not found."
                ))
        }

        treatmentPackageRepository.deleteById(id)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Treatment package with id $id deleted.",
        ))
    }
}
