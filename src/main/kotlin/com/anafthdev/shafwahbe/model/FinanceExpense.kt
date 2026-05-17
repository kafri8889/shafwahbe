package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.ExpenseKind
import com.anafthdev.shafwahbe.enums.PaymentMethod
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
@Table(name = "finance_expense")
data class FinanceExpense(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val date: LocalDate = LocalDate.now(),

    @Column(nullable = false)
    val categoryId: String = "",

    @Column(nullable = false)
    val categoryName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val kind: ExpenseKind = ExpenseKind.VARIABLE,

    @Column(nullable = false)
    val amount: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,

    @Column(nullable = false)
    val vendor: String = "",

    @Column(columnDefinition = "TEXT")
    val notes: String = "",

    @Column(nullable = false)
    val receiptUrl: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
