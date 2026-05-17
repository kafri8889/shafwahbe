package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.body.CashReconciliationRequest
import com.anafthdev.shafwahbe.model.body.FinanceCategoryRequest
import com.anafthdev.shafwahbe.model.body.FinanceExpenseRequest
import com.anafthdev.shafwahbe.model.body.MonthlyBudgetRequest
import com.anafthdev.shafwahbe.model.body.RecurringExpenseRequest
import com.anafthdev.shafwahbe.service.FinanceService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/finance")
class FinanceController(
    private val financeService: FinanceService
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
}
