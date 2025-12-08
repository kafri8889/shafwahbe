package com.anafthdev.shafwahbe.model.response

import com.anafthdev.shafwahbe.enums.AccessRole
import com.anafthdev.shafwahbe.enums.EmployeeRole
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeResponse(
    val id: Long = -1,
    val name: String = "",
    val username: String = "",
    val role: EmployeeRole = EmployeeRole.All,
    val accessRole: AccessRole = AccessRole.User,
    val phoneNumber: String = ""
)
