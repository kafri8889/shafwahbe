package com.anafthdev.shafwahbe.model.response

import com.anafthdev.shafwahbe.model.Employee

data class LoginResponse(
    val token: String,
    val user: Employee
)
