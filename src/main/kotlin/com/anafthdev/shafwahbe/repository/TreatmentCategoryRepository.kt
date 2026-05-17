package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.TreatmentCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TreatmentCategoryRepository : JpaRepository<TreatmentCategory, Long> {
    fun findFirstByTitleIgnoreCase(title: String): TreatmentCategory?
}
