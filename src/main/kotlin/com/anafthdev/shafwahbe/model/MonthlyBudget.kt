package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.ExpenseKind
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "monthly_budget")
data class MonthlyBudget(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val month: String = "",

    @Column(nullable = false)
    val categoryId: String = "",

    @Column(nullable = false)
    val categoryName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val kind: ExpenseKind = ExpenseKind.VARIABLE,

    @Column(nullable = false)
    val amount: Double = 0.0,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
