package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.VoucherDiscountType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "voucher_template")
data class VoucherTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(columnDefinition = "TEXT")
    val description: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val discountType: VoucherDiscountType = VoucherDiscountType.FIXED,

    @Column(nullable = false)
    val discountValue: Double = 0.0,

    @Column(nullable = false)
    val minimumTransaction: Double = 0.0,

    @Column(nullable = false)
    val validityDays: Int = 30,

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    val appliesToAll: Boolean = true,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "voucher_template_treatment_ids",
        joinColumns = [JoinColumn(name = "voucher_template_id")]
    )
    @Column(name = "treatment_id")
    val treatmentIds: Set<Long> = emptySet(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "voucher_template_package_ids",
        joinColumns = [JoinColumn(name = "voucher_template_id")]
    )
    @Column(name = "treatment_package_id")
    val treatmentPackageIds: Set<Long> = emptySet(),

    @Column(nullable = false)
    val active: Boolean = true,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
