package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.RecurringExpense
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecurringExpenseRepository : JpaRepository<RecurringExpense, Long> {
    fun findAllByActiveTrueOrderByNextDueDateAsc(): List<RecurringExpense>
}
