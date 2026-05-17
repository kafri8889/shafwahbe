package com.anafthdev.shafwahbe.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "staff_payroll_deduction")
data class StaffPayrollDeduction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val payroll: StaffPayroll = StaffPayroll(),

    @Column(nullable = false)
    val date: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false)
    val amount: Double = 0.0,

    @Column(columnDefinition = "TEXT")
    val notes: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
