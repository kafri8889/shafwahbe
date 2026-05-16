package com.anafthdev.shafwahbe.model.body

import java.time.LocalDate

/**
 * @property id Customer id, null if new customer
 * @property name Customer name
 * @property phoneNumber Customer phone number
 * @property birthDate Customer birth date, optional secondary identifier
 */
data class SimpleCustomerRequest(
    val id: Long?,
    val name: String,
    val phoneNumber: String,
    val birthDate: LocalDate? = null
)
