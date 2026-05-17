package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.StaffPayrollDeduction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffPayrollDeductionRepository : JpaRepository<StaffPayrollDeduction, Long> {
    fun findByPayrollIdOrderByDateDescIdDesc(payrollId: Long): List<StaffPayrollDeduction>
    fun deleteByPayrollId(payrollId: Long)
}
