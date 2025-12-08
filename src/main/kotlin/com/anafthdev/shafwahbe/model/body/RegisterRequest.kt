package com.anafthdev.shafwahbe.model.body

import com.anafthdev.shafwahbe.enums.AccessRole
import com.anafthdev.shafwahbe.enums.EmployeeRole

data class RegisterRequest(
    val name: String,
    val username: String,
    val password: String,
    val phoneNumber: String,
    val role: EmployeeRole,
    val accessRole: AccessRole,
)
