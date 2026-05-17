package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.enums.RecurringExpenseFrequency
import java.time.LocalDate

data class FinanceCategoryRequest(
    val categoryId: String,
    val name: String,
    val kind: ExpenseKind,
    val description: String = "",
    val active: Boolean = true
)

data class FinanceExpenseRequest(
    val date: LocalDate,
    val categoryId: String,
    val categoryName: String,
    val kind: ExpenseKind,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val vendor: String = "",
    val notes: String = "",
    val receiptUrl: String = ""
)

data class MonthlyBudgetRequest(
    val month: String,
    val categoryId: String,
    val categoryName: String,
    val kind: ExpenseKind,
    val amount: Double
)

data class RecurringExpenseRequest(
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val kind: ExpenseKind,
    val amount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val frequency: RecurringExpenseFrequency = RecurringExpenseFrequency.MONTHLY,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val nextDueDate: LocalDate? = null,
    val active: Boolean = true,
    val notes: String = ""
)

data class CashReconciliationRequest(
    val date: LocalDate,
    val actualCash: Double,
    val cashierName: String = "",
    val notes: String = ""
)
