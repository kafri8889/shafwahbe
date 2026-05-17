package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.FinanceCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FinanceCategoryRepository : JpaRepository<FinanceCategory, Long> {
    fun findAllByActiveTrueOrderByNameAsc(): List<FinanceCategory>
    fun findByCategoryId(categoryId: String): FinanceCategory?
    fun existsByCategoryId(categoryId: String): Boolean
}
