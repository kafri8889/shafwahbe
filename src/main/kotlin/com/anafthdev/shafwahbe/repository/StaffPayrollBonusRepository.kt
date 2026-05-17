package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.StaffPayrollBonus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffPayrollBonusRepository : JpaRepository<StaffPayrollBonus, Long> {
    fun findByPayrollIdOrderByDateDescIdDesc(payrollId: Long): List<StaffPayrollBonus>
    fun deleteByPayrollId(payrollId: Long)
}
