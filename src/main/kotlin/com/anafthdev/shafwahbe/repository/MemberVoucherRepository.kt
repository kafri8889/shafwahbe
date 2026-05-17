package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.enums.MemberVoucherStatus
import com.anafthdev.shafwahbe.model.MemberVoucher
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberVoucherRepository : JpaRepository<MemberVoucher, Long> {
    fun findByCustomerIdOrderByExpiresAtDesc(customerId: Long): List<MemberVoucher>
    fun existsByCode(code: String): Boolean
    fun findByStatus(status: MemberVoucherStatus): List<MemberVoucher>
}
