package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.MemberVoucherStatus
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
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "member_voucher")
data class MemberVoucher(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    val template: VoucherTemplate = VoucherTemplate(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer = Customer(),

    @Column(nullable = false, unique = true)
    val code: String = "",

    @Column(nullable = false)
    val assignedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now(),

    @Column(columnDefinition = "VARCHAR(255) DEFAULT ''")
    val snapshotName: String? = null,

    @Column(columnDefinition = "TEXT")
    val snapshotDescription: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    val snapshotDiscountType: VoucherDiscountType? = null,

    @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    val snapshotDiscountValue: Double? = null,

    @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 0")
    val snapshotMinimumTransaction: Double? = null,

    @Column(columnDefinition = "INTEGER DEFAULT 30")
    val snapshotValidityDays: Int? = null,

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    val snapshotAppliesToAll: Boolean? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "member_voucher_treatment_ids",
        joinColumns = [JoinColumn(name = "member_voucher_id")]
    )
    @Column(name = "treatment_id")
    val snapshotTreatmentIds: Set<Long> = emptySet(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "member_voucher_package_ids",
        joinColumns = [JoinColumn(name = "member_voucher_id")]
    )
    @Column(name = "treatment_package_id")
    val snapshotTreatmentPackageIds: Set<Long> = emptySet(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: MemberVoucherStatus = MemberVoucherStatus.ACTIVE,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_transaction_id", nullable = true)
    val usedTransaction: CustomerTransaction? = null
)
