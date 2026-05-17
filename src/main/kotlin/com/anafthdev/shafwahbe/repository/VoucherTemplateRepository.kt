package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.VoucherTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoucherTemplateRepository : JpaRepository<VoucherTemplate, Long>
