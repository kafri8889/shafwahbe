package com.anafthdev.shafwahbe.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "treatment_package")
data class TreatmentPackage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: TreatmentCategory = TreatmentCategory(),

    @Column(nullable = false)
    val title: String = "",

    @Column(nullable = false)
    val price: Double = 0.0,

    @Column(nullable = false)
    val active: Boolean = true,

    @ManyToMany
    @JoinTable(
        name = "treatment_package_items",
        joinColumns = [JoinColumn(name = "package_id")],
        inverseJoinColumns = [JoinColumn(name = "treatment_id")]
    )
    val treatments: List<Treatment> = emptyList()
)
