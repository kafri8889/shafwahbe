package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.MonthlyBudget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MonthlyBudgetRepository : JpaRepository<MonthlyBudget, Long> {
    fun findByMonthOrderByCategoryNameAsc(month: String): List<MonthlyBudget>
    fun findByMonthAndCategoryId(month: String, categoryId: String): MonthlyBudget?
}
