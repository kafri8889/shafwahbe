package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.Staff
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffRepository : JpaRepository<Staff, Long> {
    fun findAllByActiveTrueOrderByNameAsc(): List<Staff>
}
