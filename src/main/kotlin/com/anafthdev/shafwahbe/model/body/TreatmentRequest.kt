package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.PriceType

data class TreatmentRequest(
    val id: Long,
    val categoryId: Long,
    val title: String,
    val active: Boolean,
    val priceType: PriceType,
    val prices: List<Double>,
)

data class BatchTreatmentRequest(
    val treatments: List<TreatmentRequest>
)