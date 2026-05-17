package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.StaffCommissionType
import com.anafthdev.shafwahbe.enums.TreatmentType
import com.anafthdev.shafwahbe.model.*
import com.anafthdev.shafwahbe.model.body.StaffPayrollRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.StaffPayrollBonusResponse
import com.anafthdev.shafwahbe.model.response.StaffPayrollDeductionResponse
import com.anafthdev.shafwahbe.model.response.StaffPayrollResponse
import com.anafthdev.shafwahbe.repository.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*

@Service
class PayrollService(
    private val staffRepository: StaffRepository,
    private val payrollRepository: StaffPayrollRepository,
    private val payrollSettingRepository: StaffPayrollSettingRepository,
    private val payrollBonusRepository: StaffPayrollBonusRepository,
    private val payrollDeductionRepository: StaffPayrollDeductionRepository,
    private val expenseRepository: FinanceExpenseRepository,
    private val transactionRepository: CustomerTransactionRepository
) {
    private val defaultPayrollExpenseCategoryId = "salary"
    private val defaultPayrollExpenseCategoryName = "Gaji staff"
    private val defaultPayrollExpenseKind = ExpenseKind.FIXED

    fun getPayroll(month: String?): ResponseEntity<ApiResponse<List<StaffPayrollResponse>>> {
        val resolvedMonth = resolveMonth(month)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Payroll month must use YYYY-MM format."))

        val staff = staffRepository.findAllByActiveTrueOrderByNameAsc()
        val entries = payrollRepository.findByMonthOrderByStaffNameAsc(resolvedMonth).associateBy { it.staff.id }
        val stats = calculateStats(resolvedMonth)
        val rows = staff.map { buildResponse(it, resolvedMonth, entries[it.id], stats[it.id]) }

        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${rows.size} payroll rows.", data = rows))
    }

    fun getStaffPayroll(staffId: Long, month: String?): ResponseEntity<ApiResponse<StaffPayrollResponse>> {
        val resolvedMonth = resolveMonth(month)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Payroll month must use YYYY-MM format."))
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Staff with ID $staffId not found."))

        val entry = payrollRepository.findByStaffIdAndMonth(staffId, resolvedMonth)
        val stats = calculateStats(resolvedMonth)[staffId]
        return ResponseEntity.ok(ApiResponse(success = true, message = "Payroll row found.", data = buildResponse(staff, resolvedMonth, entry, stats)))
    }

    @Transactional
    fun saveStaffPayroll(staffId: Long, month: String?, request: StaffPayrollRequest): ResponseEntity<ApiResponse<StaffPayrollResponse>> {
        val resolvedMonth = resolveMonth(month)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Payroll month must use YYYY-MM format."))
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Staff with ID $staffId not found."))

        val workDays = request.workDays.coerceIn(0, YearMonth.parse(resolvedMonth).lengthOfMonth())
        val existing = payrollRepository.findByStaffIdAndMonth(staffId, resolvedMonth)
        if (existing?.paidAt != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Payroll ${staff.name} for $resolvedMonth has already been paid."))
        }
        val now = LocalDateTime.now()
        val yearMonth = YearMonth.parse(resolvedMonth)
        val saved = payrollRepository.save(
            StaffPayroll(
                id = existing?.id ?: 0,
                staff = staff,
                month = resolvedMonth,
                dailySalary = request.dailySalary.coerceAtLeast(0.0),
                workDays = workDays,
                baseSalary = request.baseSalary.coerceAtLeast(0.0),
                quarterlyReward = request.quarterlyReward.coerceAtLeast(0.0),
                quarterlyRewardStartMonth = normalizeRewardStartMonth(request.quarterlyRewardStartMonth, resolvedMonth),
                targetRevenue = request.targetRevenue.coerceAtLeast(0.0),
                targetBonusAmount = request.targetBonusAmount.coerceAtLeast(0.0),
                fridayBonusEnabled = request.fridayBonusEnabled,
                fridayBonusAmount = request.fridayBonusAmount.coerceAtLeast(0.0),
                payDate = request.payDate?.coerceInMonth(yearMonth) ?: existing?.payDate ?: defaultPayDate(yearMonth),
                paymentMethod = request.paymentMethod,
                expenseCategoryId = request.expenseCategoryId?.trim()?.takeIf { it.isNotBlank() }
                    ?: existing?.expenseCategoryId
                    ?: defaultPayrollExpenseCategoryId,
                expenseCategoryName = request.expenseCategoryName?.trim()?.takeIf { it.isNotBlank() }
                    ?: existing?.expenseCategoryName
                    ?: defaultPayrollExpenseCategoryName,
                expenseKind = request.expenseKind ?: existing?.expenseKind ?: defaultPayrollExpenseKind,
                paidAt = existing?.paidAt,
                financeExpenseId = existing?.financeExpenseId,
                notes = request.notes.trim(),
                createdAt = existing?.createdAt ?: now,
                updatedAt = now
            )
        )

        payrollBonusRepository.deleteByPayrollId(saved.id)
        val bonuses = request.bonuses
            .filter { it.title.isNotBlank() && it.amount > 0.0 }
            .map {
                StaffPayrollBonus(
                    payroll = saved,
                    date = it.date?.coerceInMonth(yearMonth) ?: saved.payDate,
                    title = it.title.trim(),
                    amount = it.amount.coerceAtLeast(0.0),
                    notes = it.notes.trim()
                )
            }
        if (bonuses.isNotEmpty()) {
            payrollBonusRepository.saveAll(bonuses)
        }

        payrollDeductionRepository.deleteByPayrollId(saved.id)
        val deductions = request.deductions
            .filter { it.title.isNotBlank() && it.amount > 0.0 }
            .map {
                StaffPayrollDeduction(
                    payroll = saved,
                    date = it.date?.coerceInMonth(yearMonth) ?: saved.payDate,
                    title = it.title.trim(),
                    amount = it.amount.coerceAtLeast(0.0),
                    notes = it.notes.trim()
                )
            }
        if (deductions.isNotEmpty()) {
            payrollDeductionRepository.saveAll(deductions)
        }

        upsertSetting(staff, saved)
        val stats = calculateStats(resolvedMonth)[staffId]
        return ResponseEntity.ok(ApiResponse(success = true, message = "Payroll for ${staff.name} saved successfully.", data = buildResponse(staff, resolvedMonth, saved, stats)))
    }

    @Transactional
    fun payStaffPayroll(staffId: Long, month: String?): ResponseEntity<ApiResponse<StaffPayrollResponse>> {
        val resolvedMonth = resolveMonth(month)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Payroll month must use YYYY-MM format."))
        val staff = staffRepository.findByIdOrNull(staffId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Staff with ID $staffId not found."))
        if (!staff.active) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Inactive staff cannot receive payroll."))
        }

        val entry = payrollRepository.findByStaffIdAndMonth(staffId, resolvedMonth)
            ?: createGeneratedPayroll(staff, resolvedMonth)
        if (entry.paidAt != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Payroll ${staff.name} for $resolvedMonth has already been paid."))
        }

        val stats = calculateStats(resolvedMonth)[staffId]
        val response = buildResponse(staff, resolvedMonth, entry, stats)
        if (response.totalPay <= 0.0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Payroll total must be greater than zero before payment."))
        }

        val expense = expenseRepository.save(
            FinanceExpense(
                date = entry.payDate,
                categoryId = entry.expenseCategoryId?.takeIf { it.isNotBlank() } ?: defaultPayrollExpenseCategoryId,
                categoryName = entry.expenseCategoryName?.takeIf { it.isNotBlank() } ?: defaultPayrollExpenseCategoryName,
                kind = entry.expenseKind ?: defaultPayrollExpenseKind,
                amount = response.totalPay,
                paymentMethod = entry.paymentMethod,
                vendor = staff.name,
                notes = "Payroll ${staff.name} bulan $resolvedMonth. Komisi otomatis: ${response.commission}. ${entry.notes}".trim(),
                receiptUrl = ""
            )
        )
        val paid = payrollRepository.save(
            entry.copy(
                paidAt = LocalDateTime.now(),
                financeExpenseId = expense.id,
                updatedAt = LocalDateTime.now()
            )
        )

        return ResponseEntity.ok(ApiResponse(success = true, message = "Payroll ${staff.name} for $resolvedMonth paid successfully.", data = buildResponse(staff, resolvedMonth, paid, stats)))
    }

    private fun upsertSetting(staff: Staff, payroll: StaffPayroll) {
        val existing = payrollSettingRepository.findByStaffId(staff.id)
        payrollSettingRepository.save(
            StaffPayrollSetting(
                id = existing?.id ?: 0,
                staff = staff,
                dailySalary = payroll.dailySalary,
                baseSalary = payroll.baseSalary,
                quarterlyRewardAmount = payroll.quarterlyReward,
                quarterlyRewardStartMonth = payroll.quarterlyRewardStartMonth,
                targetRevenue = payroll.targetRevenue,
                targetBonusAmount = payroll.targetBonusAmount,
                fridayBonusEnabled = payroll.fridayBonusEnabled,
                fridayBonusAmount = payroll.fridayBonusAmount,
                expenseCategoryId = payroll.expenseCategoryId,
                expenseCategoryName = payroll.expenseCategoryName,
                expenseKind = payroll.expenseKind,
                updatedAt = LocalDateTime.now()
            )
        )
    }

    private fun buildResponse(
        staff: Staff,
        month: String,
        entry: StaffPayroll?,
        stats: PayrollStats?
    ): StaffPayrollResponse {
        val setting = payrollSettingRepository.findByStaffId(staff.id)
        val yearMonth = YearMonth.parse(month)
        val dailySalary = entry?.dailySalary ?: setting?.dailySalary ?: 0.0
        val workDays = entry?.workDays ?: yearMonth.lengthOfMonth()
        val baseSalary = entry?.baseSalary ?: setting?.baseSalary ?: 0.0
        val quarterlyRewardAmount = entry?.quarterlyReward ?: setting?.quarterlyRewardAmount ?: 0.0
        val quarterlyRewardStartMonth = normalizeRewardStartMonth(
            entry?.quarterlyRewardStartMonth ?: setting?.quarterlyRewardStartMonth ?: "",
            month
        )
        val quarterlyRewardEligible = isQuarterlyRewardMonth(month, quarterlyRewardStartMonth)
        val quarterlyReward = if (quarterlyRewardEligible) quarterlyRewardAmount else 0.0
        val targetRevenue = entry?.targetRevenue ?: setting?.targetRevenue ?: 10_000_000.0
        val targetBonusAmount = entry?.targetBonusAmount ?: setting?.targetBonusAmount ?: 500_000.0
        val fridayBonusEnabled = entry?.fridayBonusEnabled ?: setting?.fridayBonusEnabled ?: false
        val fridayBonusAmount = entry?.fridayBonusAmount ?: setting?.fridayBonusAmount ?: 10_000.0
        val expenseCategoryId = entry?.expenseCategoryId ?: setting?.expenseCategoryId ?: defaultPayrollExpenseCategoryId
        val expenseCategoryName = entry?.expenseCategoryName ?: setting?.expenseCategoryName ?: defaultPayrollExpenseCategoryName
        val expenseKind = entry?.expenseKind ?: setting?.expenseKind ?: defaultPayrollExpenseKind
        val fridayCount = countFridays(yearMonth)
        val handledRevenue = stats?.handledRevenue ?: 0.0
        val commission = stats?.commission ?: 0.0
        val targetReached = targetRevenue > 0.0 && handledRevenue >= targetRevenue
        val targetBonusPaid = if (targetReached) targetBonusAmount else 0.0
        val fridayBonusTotal = if (fridayBonusEnabled) fridayBonusAmount * fridayCount else 0.0
        val payDate = entry?.payDate ?: defaultPayDate(yearMonth)
        val paymentMethod = entry?.paymentMethod ?: com.anafthdev.shafwahbe.enums.PaymentMethod.TRANSFER
        val dailySalaryTotal = dailySalary * workDays
        val bonuses = entry?.let { payrollBonusRepository.findByPayrollIdOrderByDateDescIdDesc(it.id) } ?: emptyList()
        val bonusResponses = bonuses.map { it.toResponse() }
        val extraBonusTotal = bonuses.sumOf { it.amount }
        val grossPay = baseSalary + dailySalaryTotal + commission + quarterlyReward + targetBonusPaid + fridayBonusTotal + extraBonusTotal
        val deductions = entry?.let { payrollDeductionRepository.findByPayrollIdOrderByDateDescIdDesc(it.id) } ?: emptyList()
        val deductionResponses = deductions.map { it.toResponse() }
        val deductionTotal = deductions.sumOf { it.amount }
        val totalPay = (grossPay - deductionTotal).coerceAtLeast(0.0)

        return StaffPayrollResponse(
            id = entry?.id,
            staff = staff,
            month = month,
            dailySalary = dailySalary,
            workDays = workDays,
            dailySalaryTotal = dailySalaryTotal,
            baseSalary = baseSalary,
            commission = commission,
            quarterlyRewardAmount = quarterlyRewardAmount,
            quarterlyRewardStartMonth = quarterlyRewardStartMonth,
            quarterlyRewardEligible = quarterlyRewardEligible,
            quarterlyReward = quarterlyReward,
            handledRevenue = handledRevenue,
            handledTransactions = stats?.handledTransactions ?: 0,
            targetRevenue = targetRevenue,
            targetReached = targetReached,
            targetBonusAmount = targetBonusAmount,
            targetBonusPaid = targetBonusPaid,
            fridayBonusEnabled = fridayBonusEnabled,
            fridayBonusAmount = fridayBonusAmount,
            fridayCount = fridayCount,
            fridayBonusTotal = fridayBonusTotal,
            payDate = payDate,
            paymentMethod = paymentMethod,
            expenseCategoryId = expenseCategoryId,
            expenseCategoryName = expenseCategoryName,
            expenseKind = expenseKind,
            paid = entry?.paidAt != null,
            paidAt = entry?.paidAt,
            financeExpenseId = entry?.financeExpenseId,
            extraBonusTotal = extraBonusTotal,
            bonuses = bonusResponses,
            grossPay = grossPay,
            deductionTotal = deductionTotal,
            deductions = deductionResponses,
            totalPay = totalPay,
            notes = entry?.notes ?: "",
            generated = entry == null
        )
    }

    private fun createGeneratedPayroll(staff: Staff, month: String): StaffPayroll {
        val yearMonth = YearMonth.parse(month)
        val setting = payrollSettingRepository.findByStaffId(staff.id)
        return payrollRepository.save(
            StaffPayroll(
                staff = staff,
                month = month,
                dailySalary = setting?.dailySalary ?: 0.0,
                workDays = yearMonth.lengthOfMonth(),
                baseSalary = setting?.baseSalary ?: 0.0,
                quarterlyReward = setting?.quarterlyRewardAmount ?: 0.0,
                quarterlyRewardStartMonth = normalizeRewardStartMonth(setting?.quarterlyRewardStartMonth ?: "", month),
                targetRevenue = setting?.targetRevenue ?: 10_000_000.0,
                targetBonusAmount = setting?.targetBonusAmount ?: 500_000.0,
                fridayBonusEnabled = setting?.fridayBonusEnabled ?: false,
                fridayBonusAmount = setting?.fridayBonusAmount ?: 10_000.0,
                payDate = defaultPayDate(yearMonth),
                expenseCategoryId = setting?.expenseCategoryId ?: defaultPayrollExpenseCategoryId,
                expenseCategoryName = setting?.expenseCategoryName ?: defaultPayrollExpenseCategoryName,
                expenseKind = setting?.expenseKind ?: defaultPayrollExpenseKind
            )
        )
    }

    private fun calculateStats(month: String): Map<Long, PayrollStats> {
        val yearMonth = YearMonth.parse(month)
        val start = yearMonth.atDay(1).atStartOfDay()
        val end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX)
        val transactions = transactionRepository.findByDateBetween(start, end)
        val stats = mutableMapOf<Long, MutablePayrollStats>()

        transactions.forEach { transaction ->
            transaction.items.forEach { item ->
                val staff = item.employee ?: transaction.employee
                val staffId = staff.id
                val current = stats.getOrPut(staffId) { MutablePayrollStats() }
                current.handledRevenue += item.price
                current.commission += calculateItemCommission(item)
                current.transactionIds.add(transaction.id)
            }
        }

        return stats.mapValues { (_, value) ->
            PayrollStats(
                handledRevenue = value.handledRevenue,
                commission = value.commission,
                handledTransactions = value.transactionIds.size
            )
        }
    }

    private fun calculateItemCommission(item: CustomerTransactionItem): Double {
        if (item.treatmentType == TreatmentType.Single && item.treatment != null) {
            val value = item.treatment.staffCommissionValue
            return if (item.treatment.staffCommissionType == StaffCommissionType.FIXED) {
                value
            } else {
                item.price * value / 100
            }
        }

        if (item.treatmentType == TreatmentType.Package && !item.treatmentPackage?.treatments.isNullOrEmpty()) {
            val treatments = item.treatmentPackage!!.treatments
            val splitPrice = item.price / treatments.size
            return treatments.sumOf { treatment ->
                val value = treatment.staffCommissionValue
                if (treatment.staffCommissionType == StaffCommissionType.FIXED) value else splitPrice * value / 100
            }
        }

        return 0.0
    }

    private fun resolveMonth(month: String?): String? {
        val value = month?.takeIf { it.isNotBlank() } ?: YearMonth.now().toString()
        return if (value.matches(Regex("\\d{4}-\\d{2}"))) value else null
    }

    private fun normalizeRewardStartMonth(value: String, fallbackMonth: String): String =
        if (value.matches(Regex("\\d{4}-\\d{2}"))) value else fallbackMonth

    private fun isQuarterlyRewardMonth(month: String, startMonth: String): Boolean {
        val current = YearMonth.parse(month)
        val start = YearMonth.parse(startMonth)
        if (current.isBefore(start)) return false
        val monthDistance = (current.year - start.year) * 12 + current.monthValue - start.monthValue
        return monthDistance % 3 == 0
    }

    private fun countFridays(month: YearMonth): Int {
        var date: LocalDate = month.atDay(1)
        val end = month.atEndOfMonth()
        var count = 0
        while (!date.isAfter(end)) {
            if (date.dayOfWeek == DayOfWeek.FRIDAY) count++
            date = date.plusDays(1)
        }
        return count
    }

    private fun defaultPayDate(month: YearMonth): LocalDate = month.atEndOfMonth()

    private fun StaffPayrollDeduction.toResponse() = StaffPayrollDeductionResponse(
        id = id,
        date = date,
        title = title,
        amount = amount,
        notes = notes
    )

    private fun StaffPayrollBonus.toResponse() = StaffPayrollBonusResponse(
        id = id,
        date = date,
        title = title,
        amount = amount,
        notes = notes
    )

    private fun LocalDate.coerceInMonth(month: YearMonth): LocalDate {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return when {
            isBefore(start) -> start
            isAfter(end) -> end
            else -> this
        }
    }

    private data class MutablePayrollStats(
        var handledRevenue: Double = 0.0,
        var commission: Double = 0.0,
        val transactionIds: MutableSet<Long> = mutableSetOf()
    )

    private data class PayrollStats(
        val handledRevenue: Double,
        val commission: Double,
        val handledTransactions: Int
    )
}
