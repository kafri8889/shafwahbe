package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.CashReconciliationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "cash_reconciliation")
data class CashReconciliation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val date: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val expectedCash: Double = 0.0,

    @Column(nullable = false)
    val actualCash: Double = 0.0,

    @Column(nullable = false)
    val difference: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: CashReconciliationStatus = CashReconciliationStatus.BALANCED,

    @Column(nullable = false)
    val cashierName: String = "",

    @Column(columnDefinition = "TEXT")
    val notes: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
