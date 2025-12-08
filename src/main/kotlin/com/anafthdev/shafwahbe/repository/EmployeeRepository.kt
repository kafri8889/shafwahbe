package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.enums.EmployeeRole
import com.anafthdev.shafwahbe.model.Employee
import org.springframework.data.jpa.repository.JpaRepository

interface EmployeeRepository : JpaRepository<Employee, Long> {
    fun findByUsername(username: String): Employee?
    fun existsByUsername(username: String): Boolean
    fun findAllByRole(role: EmployeeRole): List<Employee>
    fun deleteByUsername(username: String)
}