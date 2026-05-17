package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.StaffPayrollSetting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffPayrollSettingRepository : JpaRepository<StaffPayrollSetting, Long> {
    fun findByStaffId(staffId: Long): StaffPayrollSetting?
}
