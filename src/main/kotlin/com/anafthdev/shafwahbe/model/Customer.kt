package com.anafthdev.shafwahbe.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * @property visitCount Jumlah kunjungan (reset per tahun)
 * @property totalVisitCount Jumlah kunjungan lifetime
 */
@Entity
@Table(name = "customer")
data class Customer(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = true, unique = false)
    val phoneNumber: String = "",

    @Column(nullable = true)
    val address: String = "",

    @Column(nullable = true)
    val birthDate: LocalDate? = null,

    @Column(nullable = false)
    val visitCount: Int = 0,

    @Column(nullable = false)
    val totalVisitCount: Int = 0,

    @Column(nullable = false)
    val lastVisitDate: LocalDateTime = LocalDateTime.now()
)
