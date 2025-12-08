package com.anafthdev.shafwahbe.model

import com.anafthdev.shafwahbe.enums.AccessRole
import com.anafthdev.shafwahbe.enums.EmployeeRole
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import kotlinx.serialization.Serializable

/**
 * @property phoneNumber Format: 08xxxxxxxxxx
 * @property username Username untuk login
 * @property password Password untuk login
 */
@Entity
@Table(name = "employee")
@Serializable
data class Employee @JvmOverloads constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false, unique = true)
    val username: String = "",

    @Column(nullable = false)
    val password: String = "",

    @Enumerated(EnumType.STRING)
    val role: EmployeeRole = EmployeeRole.All,

    @Enumerated(EnumType.STRING)
    val accessRole: AccessRole = AccessRole.User,

    @Column(nullable = false)
    val phoneNumber: String = ""
)
