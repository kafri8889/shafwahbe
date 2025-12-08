package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CustomerRepository : JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE CONCAT('%', LOWER(:name), '%')")
    fun findByNameIgnoreCase(@Param("name") name: String): List<Customer>

    fun findByVisitCountBetween(min: Int, max: Int): List<Customer>

    fun findByTotalVisitCountBetween(min: Int, max: Int): List<Customer>

    @Query(
        "SELECT c FROM Customer c WHERE c.lastVisitDate BETWEEN :start AND :end"
    )
    fun findByLastVisitDateBetween(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<Customer>
}
