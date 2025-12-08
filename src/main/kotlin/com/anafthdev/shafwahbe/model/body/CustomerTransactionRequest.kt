package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.TreatmentType
import com.anafthdev.shafwahbe.enums.PaymentMethod
import java.time.LocalDateTime

data class CustomerTransactionItemRequest(
    val treatmentType: TreatmentType,
    val treatmentId: Long?,
    val treatmentPackageId: Long?,
    val price: Double
)

data class CustomerTransactionRequest(
    val employeeId: Long,
    val customer: SimpleCustomerRequest,
    val items: List<CustomerTransactionItemRequest>,
    val actualPrice: Double,
    val paymentMethod: PaymentMethod,
    val notes: String = "",
    val date: LocalDateTime
)
