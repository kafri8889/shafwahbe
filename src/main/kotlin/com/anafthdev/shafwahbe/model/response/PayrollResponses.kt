package com.anafthdev.shafwahbe.model.response

import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.Staff
import java.time.LocalDate
import java.time.LocalDateTime

data class StaffPayrollResponse(
    val id: Long?,
    val staff: Staff,
    val month: String,
    val dailySalary: Double,
    val workDays: Int,
    val dailySalaryTotal: Double,
    val baseSalary: Double,
    val commission: Double,
    val quarterlyRewardAmount: Double,
    val quarterlyRewardStartMonth: String,
    val quarterlyRewardEligible: Boolean,
    val quarterlyReward: Double,
    val handledRevenue: Double,
    val handledTransactions: Int,
    val targetRevenue: Double,
    val targetReached: Boolean,
    val targetBonusAmount: Double,
    val targetBonusPaid: Double,
    val fridayBonusEnabled: Boolean,
    val fridayBonusAmount: Double,
    val fridayCount: Int,
    val fridayBonusTotal: Double,
    val payDate: LocalDate,
    val paymentMethod: PaymentMethod,
    val expenseCategoryId: String,
    val expenseCategoryName: String,
    val expenseKind: ExpenseKind,
    val paid: Boolean,
    val paidAt: LocalDateTime?,
    val financeExpenseId: Long?,
    val extraBonusTotal: Double,
    val bonuses: List<StaffPayrollBonusResponse>,
    val grossPay: Double,
    val deductionTotal: Double,
    val deductions: List<StaffPayrollDeductionResponse>,
    val totalPay: Double,
    val notes: String,
    val generated: Boolean
)

data class StaffPayrollBonusResponse(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val amount: Double,
    val notes: String
)

data class StaffPayrollDeductionResponse(
    val id: Long,
    val date: LocalDate,
    val title: String,
    val amount: Double,
    val notes: String
)
