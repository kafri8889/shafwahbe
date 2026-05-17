package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.enums.CashReconciliationStatus
import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.CashReconciliation
import com.anafthdev.shafwahbe.model.FinanceCategory
import com.anafthdev.shafwahbe.model.FinanceExpense
import com.anafthdev.shafwahbe.model.MonthlyBudget
import com.anafthdev.shafwahbe.model.RecurringExpense
import com.anafthdev.shafwahbe.model.body.CashReconciliationRequest
import com.anafthdev.shafwahbe.model.body.FinanceCategoryRequest
import com.anafthdev.shafwahbe.model.body.FinanceExpenseRequest
import com.anafthdev.shafwahbe.model.body.MonthlyBudgetRequest
import com.anafthdev.shafwahbe.model.body.RecurringExpenseRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.CashReconciliationRepository
import com.anafthdev.shafwahbe.repository.CustomerTransactionRepository
import com.anafthdev.shafwahbe.repository.FinanceCategoryRepository
import com.anafthdev.shafwahbe.repository.FinanceExpenseRepository
import com.anafthdev.shafwahbe.repository.MonthlyBudgetRepository
import com.anafthdev.shafwahbe.repository.RecurringExpenseRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class FinanceService(
    private val categoryRepository: FinanceCategoryRepository,
    private val expenseRepository: FinanceExpenseRepository,
    private val budgetRepository: MonthlyBudgetRepository,
    private val recurringExpenseRepository: RecurringExpenseRepository,
    private val cashReconciliationRepository: CashReconciliationRepository,
    private val transactionRepository: CustomerTransactionRepository
) {

    fun getCategories(): ResponseEntity<ApiResponse<List<FinanceCategory>>> {
        ensureDefaultCategories()
        val categories = categoryRepository.findAllByActiveTrueOrderByNameAsc()
        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${categories.size} finance categories.", data = categories))
    }

    fun createCategory(request: FinanceCategoryRequest): ResponseEntity<ApiResponse<FinanceCategory>> {
        val categoryId = normalizeCategoryId(request.categoryId.ifBlank { request.name })
        if (categoryId.isBlank() || request.name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Category ID and name are required."))
        }
        if (categoryRepository.existsByCategoryId(categoryId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Category ID '$categoryId' already exists."))
        }

        val saved = categoryRepository.save(request.toCategory(categoryId = categoryId))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Finance category '${saved.name}' created successfully.", data = saved))
    }

    fun updateCategory(id: Long, request: FinanceCategoryRequest): ResponseEntity<ApiResponse<FinanceCategory>> {
        val existing = categoryRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Finance category with ID $id not found."))

        val categoryId = normalizeCategoryId(request.categoryId.ifBlank { request.name })
        if (categoryId.isBlank() || request.name.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Category ID and name are required."))
        }

        val duplicate = categoryRepository.findByCategoryId(categoryId)
        if (duplicate != null && duplicate.id != id) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Category ID '$categoryId' already exists."))
        }

        val saved = categoryRepository.save(request.toCategory(id = existing.id, categoryId = categoryId, createdAt = existing.createdAt))
        return ResponseEntity.ok(ApiResponse(success = true, message = "Finance category '${saved.name}' updated successfully.", data = saved))
    }

    fun deleteCategory(id: Long): ResponseEntity<ApiResponse<FinanceCategory>> {
        val existing = categoryRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Finance category with ID $id not found."))

        val saved = categoryRepository.save(existing.copy(active = false))
        return ResponseEntity.ok(ApiResponse(success = true, message = "Finance category '${saved.name}' deactivated successfully.", data = saved))
    }

    fun getExpenses(startDate: LocalDate?, endDate: LocalDate?): ResponseEntity<ApiResponse<List<FinanceExpense>>> {
        val expenses = if (startDate != null && endDate != null) {
            expenseRepository.findByDateBetweenOrderByDateDesc(startDate, endDate)
        } else {
            expenseRepository.findAll().sortedByDescending { it.date }
        }

        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${expenses.size} expenses.", data = expenses))
    }

    fun createExpense(request: FinanceExpenseRequest): ResponseEntity<ApiResponse<FinanceExpense>> {
        val validation = validateExpense(request.amount, request.categoryName)
        if (validation != null) return validation

        val saved = expenseRepository.save(request.toExpense())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Expense '${saved.categoryName}' created successfully.", data = saved))
    }

    fun updateExpense(id: Long, request: FinanceExpenseRequest): ResponseEntity<ApiResponse<FinanceExpense>> {
        val existing = expenseRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Expense with ID $id not found."))

        val validation = validateExpense(request.amount, request.categoryName)
        if (validation != null) return validation

        val saved = expenseRepository.save(request.toExpense(existing.id, existing.createdAt))
        return ResponseEntity.ok(ApiResponse(success = true, message = "Expense with ID $id updated successfully.", data = saved))
    }

    fun deleteExpense(id: Long): ResponseEntity<ApiResponse<Unit>> {
        if (!expenseRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Expense with ID $id not found."))
        }

        expenseRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Expense with ID $id deleted successfully."))
    }

    fun getBudgets(month: String?): ResponseEntity<ApiResponse<List<MonthlyBudget>>> {
        val budgets = if (month.isNullOrBlank()) {
            budgetRepository.findAll().sortedWith(compareBy<MonthlyBudget> { it.month }.thenBy { it.categoryName })
        } else {
            budgetRepository.findByMonthOrderByCategoryNameAsc(month)
        }

        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${budgets.size} monthly budgets.", data = budgets))
    }

    fun saveBudget(request: MonthlyBudgetRequest): ResponseEntity<ApiResponse<MonthlyBudget>> {
        if (!request.month.matches(Regex("\\d{4}-\\d{2}"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Budget month must use YYYY-MM format."))
        }
        if (request.categoryName.isBlank() || request.amount < 0.0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Budget category and amount are required."))
        }

        val existing = budgetRepository.findByMonthAndCategoryId(request.month, request.categoryId)
        val saved = budgetRepository.save(
            MonthlyBudget(
                id = existing?.id ?: 0,
                month = request.month,
                categoryId = request.categoryId.trim(),
                categoryName = request.categoryName.trim(),
                kind = request.kind,
                amount = request.amount,
                createdAt = existing?.createdAt ?: java.time.LocalDateTime.now()
            )
        )

        return ResponseEntity.ok(ApiResponse(success = true, message = "Budget '${saved.categoryName}' saved successfully.", data = saved))
    }

    fun deleteBudget(id: Long): ResponseEntity<ApiResponse<Unit>> {
        if (!budgetRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Budget with ID $id not found."))
        }

        budgetRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Budget with ID $id deleted successfully."))
    }

    fun getRecurringExpenses(): ResponseEntity<ApiResponse<List<RecurringExpense>>> {
        val items = recurringExpenseRepository.findAll().sortedBy { it.nextDueDate }
        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${items.size} recurring expenses.", data = items))
    }

    fun createRecurringExpense(request: RecurringExpenseRequest): ResponseEntity<ApiResponse<RecurringExpense>> {
        val validation = validateRecurring(request)
        if (validation != null) return validation

        val saved = recurringExpenseRepository.save(request.toRecurringExpense())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Recurring expense '${saved.name}' created successfully.", data = saved))
    }

    fun updateRecurringExpense(id: Long, request: RecurringExpenseRequest): ResponseEntity<ApiResponse<RecurringExpense>> {
        val existing = recurringExpenseRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Recurring expense with ID $id not found."))

        val validation = validateRecurring(request)
        if (validation != null) return validation

        val saved = recurringExpenseRepository.save(request.toRecurringExpense(existing.id, existing.createdAt))
        return ResponseEntity.ok(ApiResponse(success = true, message = "Recurring expense with ID $id updated successfully.", data = saved))
    }

    fun deactivateRecurringExpense(id: Long): ResponseEntity<ApiResponse<RecurringExpense>> {
        val existing = recurringExpenseRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Recurring expense with ID $id not found."))

        val saved = recurringExpenseRepository.save(existing.copy(active = false))
        return ResponseEntity.ok(ApiResponse(success = true, message = "Recurring expense with ID $id deactivated successfully.", data = saved))
    }

    fun getCashReconciliations(startDate: LocalDate?, endDate: LocalDate?): ResponseEntity<ApiResponse<List<CashReconciliation>>> {
        val items = if (startDate != null && endDate != null) {
            cashReconciliationRepository.findByDateBetweenOrderByDateDesc(startDate, endDate)
        } else {
            cashReconciliationRepository.findAll().sortedByDescending { it.date }
        }

        return ResponseEntity.ok(ApiResponse(success = true, message = "Found ${items.size} cash reconciliations.", data = items))
    }

    fun calculateExpectedCash(date: LocalDate): ResponseEntity<ApiResponse<Double>> {
        return ResponseEntity.ok(ApiResponse(success = true, message = "Expected cash calculated.", data = expectedCashForDate(date)))
    }

    fun createCashReconciliation(request: CashReconciliationRequest): ResponseEntity<ApiResponse<CashReconciliation>> {
        val expectedCash = expectedCashForDate(request.date)
        val difference = request.actualCash - expectedCash
        val status = when {
            kotlin.math.abs(difference) < 0.01 -> CashReconciliationStatus.BALANCED
            difference > 0.0 -> CashReconciliationStatus.OVER
            else -> CashReconciliationStatus.SHORT
        }

        val saved = cashReconciliationRepository.save(
            CashReconciliation(
                date = request.date,
                expectedCash = expectedCash,
                actualCash = request.actualCash.coerceAtLeast(0.0),
                difference = difference,
                status = status,
                cashierName = request.cashierName.trim(),
                notes = request.notes.trim()
            )
        )

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Cash reconciliation created successfully.", data = saved))
    }

    private fun expectedCashForDate(date: LocalDate): Double {
        val cashRevenue = transactionRepository.findByPaymentMethodAndDateBetween(PaymentMethod.CASH, date, date)
            .sumOf { it.actualPrice }
        val cashExpenses = expenseRepository.findByPaymentMethodAndDateBetween(PaymentMethod.CASH, date, date)
            .sumOf { it.amount }

        return cashRevenue - cashExpenses
    }

    private fun ensureDefaultCategories() {
        defaultCategories.forEach { category ->
            if (!categoryRepository.existsByCategoryId(category.categoryId)) {
                categoryRepository.save(category)
            }
        }
    }

    private fun normalizeCategoryId(value: String): String =
        value.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

    private fun validateExpense(amount: Double, categoryName: String): ResponseEntity<ApiResponse<FinanceExpense>>? {
        if (categoryName.isBlank() || amount <= 0.0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Expense category and positive amount are required."))
        }
        return null
    }

    private fun FinanceCategoryRequest.toCategory(
        id: Long = 0,
        categoryId: String,
        createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
    ) = FinanceCategory(
        id = id,
        categoryId = categoryId,
        name = name.trim(),
        kind = kind,
        description = description.trim(),
        active = active,
        createdAt = createdAt
    )

    private fun validateRecurring(request: RecurringExpenseRequest): ResponseEntity<ApiResponse<RecurringExpense>>? {
        if (request.name.isBlank() || request.categoryName.isBlank() || request.amount <= 0.0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Recurring expense name, category, and positive amount are required."))
        }
        val startDate = request.resolvedStartDate()
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Recurring expense start date is required."))
        if (request.endDate != null && request.endDate.isBefore(startDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Recurring expense end date cannot be before start date."))
        }
        return null
    }

    private fun RecurringExpenseRequest.resolvedStartDate(): LocalDate? = startDate ?: nextDueDate

    private fun FinanceExpenseRequest.toExpense(
        id: Long = 0,
        createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
    ) = FinanceExpense(
        id = id,
        date = date,
        categoryId = categoryId.trim(),
        categoryName = categoryName.trim(),
        kind = kind,
        amount = amount,
        paymentMethod = paymentMethod,
        vendor = vendor.trim(),
        notes = notes.trim(),
        receiptUrl = receiptUrl.trim(),
        createdAt = createdAt
    )

    private fun RecurringExpenseRequest.toRecurringExpense(
        id: Long = 0,
        createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
    ): RecurringExpense {
        val dueDate = resolvedStartDate() ?: LocalDate.now()
        return RecurringExpense(
        id = id,
        name = name.trim(),
        categoryId = categoryId.trim(),
        categoryName = categoryName.trim(),
        kind = kind,
        amount = amount,
        paymentMethod = paymentMethod,
        frequency = frequency,
        startDate = dueDate,
        endDate = endDate,
        nextDueDate = dueDate,
        active = active,
        notes = notes.trim(),
        createdAt = createdAt
    )
    }

    companion object {
        private val defaultCategories = listOf(
            FinanceCategory(categoryId = "products", name = "Produk & bahan", kind = ExpenseKind.VARIABLE, description = "Cream, shampoo, masker, obat treatment, consumable salon."),
            FinanceCategory(categoryId = "salary", name = "Gaji non-komisi", kind = ExpenseKind.FIXED, description = "Gaji pokok, uang makan, allowance, atau payroll di luar komisi."),
            FinanceCategory(categoryId = "rent", name = "Sewa & tempat", kind = ExpenseKind.FIXED, description = "Sewa tempat, IPL, maintenance ruangan, dan biaya tempat lain."),
            FinanceCategory(categoryId = "utilities", name = "Listrik, air, internet", kind = ExpenseKind.FIXED, description = "Tagihan rutin operasional salon."),
            FinanceCategory(categoryId = "marketing", name = "Marketing & promo", kind = ExpenseKind.VARIABLE, description = "Iklan, konten, endorsement, diskon campaign di luar voucher."),
            FinanceCategory(categoryId = "petty_cash", name = "Petty cash", kind = ExpenseKind.ONE_TIME, description = "Belanja kecil harian dan koreksi kas.")
        )
    }
}
