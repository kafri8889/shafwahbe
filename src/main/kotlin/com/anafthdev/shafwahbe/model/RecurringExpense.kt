package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.enums.RecurringExpenseFrequency
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
@Table(name = "recurring_expense")
data class RecurringExpense(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val categoryId: String = "",

    @Column(nullable = false)
    val categoryName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val kind: ExpenseKind = ExpenseKind.FIXED,

    @Column(nullable = false)
    val amount: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val frequency: RecurringExpenseFrequency = RecurringExpenseFrequency.MONTHLY,

    val startDate: LocalDate? = null,

    val endDate: LocalDate? = null,

    @Column(nullable = false)
    val nextDueDate: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(columnDefinition = "TEXT")
    val notes: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
