package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.StaffPayroll
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffPayrollRepository : JpaRepository<StaffPayroll, Long> {
    fun findByMonthOrderByStaffNameAsc(month: String): List<StaffPayroll>
    fun findByStaffIdAndMonth(staffId: Long, month: String): StaffPayroll?
}
