package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.CustomerTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CustomerTransactionRepository :
    JpaRepository<CustomerTransaction, Long>,
    JpaSpecificationExecutor<CustomerTransaction> {

    /**
     * Used internally by [com.anafthdev.shafwahbe.service.FinanceService.expectedCashForDate]
     * to compute the expected cash balance for a single day.
     */
    fun findByPaymentMethodAndDateBetween(
        paymentMethod: PaymentMethod,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CustomerTransaction>

    fun findByDateBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<CustomerTransaction>
}
