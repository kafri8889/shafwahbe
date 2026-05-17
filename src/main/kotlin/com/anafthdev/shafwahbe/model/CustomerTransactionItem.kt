package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.TreatmentType
import jakarta.persistence.Column
import jakarta.persistence.ConstraintMode
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "customer_transaction_item")
data class CustomerTransactionItem(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    val treatment: Treatment? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_package_id")
    val treatmentPackage: TreatmentPackage? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    val employee: Staff? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val treatmentType: TreatmentType = TreatmentType.Single,

    @Column(nullable = false)
    val price: Double = 0.0
)
