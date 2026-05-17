package com.anafthdev.shafwahbe.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "staff")
data class Staff(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val role: String = "Stylist",

    @Column(nullable = false)
    val phoneNumber: String = "",

    @Column(nullable = false)
    val active: Boolean = true
)
