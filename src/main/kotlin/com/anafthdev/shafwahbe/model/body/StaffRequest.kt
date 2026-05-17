package com.anafthdev.shafwahbe.model.body

data class StaffRequest(
    val name: String,
    val role: String = "Stylist",
    val phoneNumber: String = "",
    val active: Boolean = true
)
