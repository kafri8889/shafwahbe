package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.CashReconciliation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CashReconciliationRepository : JpaRepository<CashReconciliation, Long> {
    fun findByDateBetweenOrderByDateDesc(startDate: LocalDate, endDate: LocalDate): List<CashReconciliation>
}
