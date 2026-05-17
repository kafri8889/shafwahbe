package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.PaymentMethod
import java.time.LocalDate

data class StaffPayrollRequest(
    val dailySalary: Double = 0.0,
    val workDays: Int = 0,
    val baseSalary: Double = 0.0,
    val quarterlyReward: Double = 0.0,
    val quarterlyRewardStartMonth: String = "",
    val targetRevenue: Double = 10_000_000.0,
    val targetBonusAmount: Double = 500_000.0,
    val fridayBonusEnabled: Boolean = false,
    val fridayBonusAmount: Double = 10_000.0,
    val payDate: LocalDate? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.TRANSFER,
    val expenseCategoryId: String? = null,
    val expenseCategoryName: String? = null,
    val expenseKind: ExpenseKind? = null,
    val bonuses: List<StaffPayrollBonusRequest> = emptyList(),
    val deductions: List<StaffPayrollDeductionRequest> = emptyList(),
    val notes: String = ""
)

data class StaffPayrollBonusRequest(
    val date: LocalDate? = null,
    val title: String = "",
    val amount: Double = 0.0,
    val notes: String = ""
)

data class StaffPayrollDeductionRequest(
    val date: LocalDate? = null,
    val title: String = "",
    val amount: Double = 0.0,
    val notes: String = ""
)
