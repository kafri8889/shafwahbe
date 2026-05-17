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
@Table(name = "finance_category")
data class FinanceCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val categoryId: String = "",

    @Column(nullable = false)
    val name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val kind: ExpenseKind = ExpenseKind.VARIABLE,

    @Column(columnDefinition = "TEXT")
    val description: String = "",

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
