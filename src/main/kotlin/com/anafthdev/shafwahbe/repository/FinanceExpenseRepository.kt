package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.FinanceExpense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface FinanceExpenseRepository : JpaRepository<FinanceExpense, Long> {
    fun findByDateBetweenOrderByDateDesc(startDate: LocalDate, endDate: LocalDate): List<FinanceExpense>
    fun findByPaymentMethodAndDateBetween(paymentMethod: PaymentMethod, startDate: LocalDate, endDate: LocalDate): List<FinanceExpense>
}
