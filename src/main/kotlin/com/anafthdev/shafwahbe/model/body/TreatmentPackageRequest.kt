package com.anafthdev.shafwahbe.model.body

data class TreatmentPackageRequest(
    val title: String,
    val price: Double,
    val active: Boolean,
    val categoryId: Long,
    val treatmentIds: List<Long>
)

data class BatchTreatmentPackageRequest(
    val packages: List<TreatmentPackageRequest>
)
