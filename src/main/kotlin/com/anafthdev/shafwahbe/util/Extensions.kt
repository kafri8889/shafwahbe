package com.anafthdev.shafwahbe.util

import com.anafthdev.shafwahbe.model.Employee
import com.anafthdev.shafwahbe.model.response.EmployeeResponse

fun Employee.toEmployeeResponse(): EmployeeResponse = EmployeeResponse(
    id = this.id,
    name = this.name,
    username = this.username,
    role = this.role,
    accessRole = this.accessRole,
    phoneNumber = this.phoneNumber
)
