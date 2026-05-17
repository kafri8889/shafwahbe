package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.body.*
import com.anafthdev.shafwahbe.service.FinanceService
import com.anafthdev.shafwahbe.service.PayrollService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/finance")
class FinanceController(
    private val financeService: FinanceService,
    private val payrollService: PayrollService
) {

    @GetMapping("/categories")
    fun getCategories() = financeService.getCategories()

    @PostMapping("/categories")
    fun createCategory(@RequestBody request: FinanceCategoryRequest) = financeService.createCategory(request)

    @PutMapping("/categories/{id}")
    fun updateCategory(@PathVariable id: Long, @RequestBody request: FinanceCategoryRequest) =
        financeService.updateCategory(id, request)

    @DeleteMapping("/categories/{id}")
    fun deleteCategory(@PathVariable id: Long) = financeService.deleteCategory(id)

    @GetMapping("/expenses")
    fun getExpenses(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ) = financeService.getExpenses(startDate, endDate)

    @PostMapping("/expenses")
    fun createExpense(@RequestBody request: FinanceExpenseRequest) = financeService.createExpense(request)

    @PutMapping("/expenses/{id}")
    fun updateExpense(@PathVariable id: Long, @RequestBody request: FinanceExpenseRequest) =
        financeService.updateExpense(id, request)

    @DeleteMapping("/expenses/{id}")
    fun deleteExpense(@PathVariable id: Long) = financeService.deleteExpense(id)

    @GetMapping("/budgets")
    fun getBudgets(@RequestParam(required = false) month: String?) = financeService.getBudgets(month)

    @PostMapping("/budgets")
    fun saveBudget(@RequestBody request: MonthlyBudgetRequest) = financeService.saveBudget(request)

    @DeleteMapping("/budgets/{id}")
    fun deleteBudget(@PathVariable id: Long) = financeService.deleteBudget(id)

    @GetMapping("/recurring-expenses")
    fun getRecurringExpenses() = financeService.getRecurringExpenses()

    @PostMapping("/recurring-expenses")
    fun createRecurringExpense(@RequestBody request: RecurringExpenseRequest) =
        financeService.createRecurringExpense(request)

    @PutMapping("/recurring-expenses/{id}")
    fun updateRecurringExpense(@PathVariable id: Long, @RequestBody request: RecurringExpenseRequest) =
        financeService.updateRecurringExpense(id, request)

    @PatchMapping("/recurring-expenses/{id}/deactivate")
    fun deactivateRecurringExpense(@PathVariable id: Long) = financeService.deactivateRecurringExpense(id)

    @GetMapping("/cash-reconciliations")
    fun getCashReconciliations(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?
    ) = financeService.getCashReconciliations(startDate, endDate)

    @GetMapping("/cash-reconciliations/expected")
    fun calculateExpectedCash(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ) = financeService.calculateExpectedCash(date)

    @PostMapping("/cash-reconciliations")
    fun createCashReconciliation(@RequestBody request: CashReconciliationRequest) =
        financeService.createCashReconciliation(request)

    @GetMapping("/payroll")
    fun getPayroll(@RequestParam(required = false) month: String?) = payrollService.getPayroll(month)

    @GetMapping("/payroll/{staffId}")
    fun getStaffPayroll(
        @PathVariable staffId: Long,
        @RequestParam(required = false) month: String?
    ) = payrollService.getStaffPayroll(staffId, month)

    @PutMapping("/payroll/{staffId}")
    fun saveStaffPayroll(
        @PathVariable staffId: Long,
        @RequestParam(required = false) month: String?,
        @RequestBody request: StaffPayrollRequest
    ) = payrollService.saveStaffPayroll(staffId, month, request)

    @PatchMapping("/payroll/{staffId}/pay")
    fun payStaffPayroll(
        @PathVariable staffId: Long,
        @RequestParam(required = false) month: String?
    ) = payrollService.payStaffPayroll(staffId, month)
}
