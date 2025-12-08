package com.anafthdev.shafwahbe.model.body

/**
 * @property id Customer id, null if new customer
 * @property name Customer name
 * @property phoneNumber Customer phone number
 */
data class SimpleCustomerRequest(
    val id: Long?,
    val name: String,
    val phoneNumber: String
)
