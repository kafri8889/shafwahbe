package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.Treatment
import org.springframework.data.jpa.repository.JpaRepository

interface TreatmentRepository : JpaRepository<Treatment, Long> {
    fun findAllByCategoryId(categoryId: Long): List<Treatment>
    fun findAllByActive(active: Boolean): List<Treatment>
}
