package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.ExpenseKind
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "staff_payroll_setting")
data class StaffPayrollSetting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false, unique = true, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val staff: Staff = Staff(),

    @Column(nullable = false)
    val dailySalary: Double = 0.0,

    @Column(nullable = false)
    val baseSalary: Double = 0.0,

    @Column(nullable = false)
    val quarterlyRewardAmount: Double = 0.0,

    @Column(nullable = true, length = 7)
    val quarterlyRewardStartMonth: String? = null,

    @Column(nullable = false)
    val targetRevenue: Double = 10_000_000.0,

    @Column(nullable = false)
    val targetBonusAmount: Double = 500_000.0,

    @Column(nullable = false)
    val fridayBonusEnabled: Boolean = false,

    @Column(nullable = false)
    val fridayBonusAmount: Double = 10_000.0,

    @Column(nullable = true)
    val expenseCategoryId: String? = null,

    @Column(nullable = true)
    val expenseCategoryName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    val expenseKind: ExpenseKind? = null,

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
