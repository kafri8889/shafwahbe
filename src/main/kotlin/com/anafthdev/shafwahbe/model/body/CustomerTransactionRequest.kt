package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.TreatmentType
import com.anafthdev.shafwahbe.enums.PaymentMethod
import java.time.LocalDateTime

data class CustomerTransactionItemRequest(
    val treatmentType: TreatmentType,
    val treatmentId: Long?,
    val treatmentPackageId: Long?,
    val price: Double,
    val employeeId: Long? = null
)

data class CustomerTransactionRequest(
    /**
     * Backward-compatible field for stylist/staff. It no longer represents the cashier login account.
     */
    val employeeId: Long,
    val customer: SimpleCustomerRequest,
    val items: List<CustomerTransactionItemRequest>,
    val actualPrice: Double,
    val paymentMethod: PaymentMethod,
    val notes: String = "",
    val date: LocalDateTime,
    val memberVoucherId: Long? = null
)

data class LegacyTransactionRequest(
    val employeeId: Long,
    val commissionPercent: Double,
    val actualPrice: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val notes: String = "",
    val date: LocalDateTime
)
