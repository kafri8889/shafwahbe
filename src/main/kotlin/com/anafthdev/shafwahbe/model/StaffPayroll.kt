package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.PaymentMethod
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "staff_payroll",
    uniqueConstraints = [UniqueConstraint(name = "uk_staff_payroll_staff_month", columnNames = ["staff_id", "month"])]
)
data class StaffPayroll(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val staff: Staff = Staff(),

    @Column(nullable = false, length = 7)
    val month: String = "",

    @Column(nullable = false)
    val dailySalary: Double = 0.0,

    @Column(nullable = false)
    val workDays: Int = 0,

    @Column(nullable = false)
    val baseSalary: Double = 0.0,

    @Column(nullable = false)
    val quarterlyReward: Double = 0.0,

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

    @Column(nullable = false)
    val payDate: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod = PaymentMethod.TRANSFER,

    @Column(nullable = true)
    val expenseCategoryId: String? = null,

    @Column(nullable = true)
    val expenseCategoryName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    val expenseKind: ExpenseKind? = null,

    @Column(nullable = true)
    val paidAt: LocalDateTime? = null,

    @Column(nullable = true)
    val financeExpenseId: Long? = null,

    @Column(columnDefinition = "TEXT")
    val notes: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
