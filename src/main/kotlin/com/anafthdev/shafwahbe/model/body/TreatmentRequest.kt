package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.PriceType
import com.anafthdev.shafwahbe.enums.StaffCommissionType

data class TreatmentRequest(
    val id: Long,
    val categoryId: Long,
    val title: String,
    val active: Boolean,
    val priceType: PriceType,
    val prices: List<Double>,
    val staffCommissionType: StaffCommissionType = StaffCommissionType.PERCENTAGE,
    val staffCommissionValue: Double = 0.0
)

data class BatchTreatmentRequest(
    val treatments: List<TreatmentRequest>
)
