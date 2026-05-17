package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.VoucherDiscountType

data class VoucherTemplateRequest(
    val name: String,
    val description: String = "",
    val discountType: VoucherDiscountType,
    val discountValue: Double,
    val minimumTransaction: Double = 0.0,
    val validityDays: Int = 30,
    val appliesToAll: Boolean = true,
    val treatmentIds: Set<Long> = emptySet(),
    val treatmentPackageIds: Set<Long> = emptySet(),
    val active: Boolean = true
)

data class AssignVoucherRequest(
    val templateId: Long,
    val customerId: Long
)
