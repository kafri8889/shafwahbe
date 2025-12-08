package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.Customer
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.service.CustomerService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse<List<Customer>>> = customerService.getAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<Customer>> = customerService.getById(id)

    @PostMapping
    fun create(@RequestBody customer: Customer): ResponseEntity<ApiResponse<Customer>> = customerService.create(customer)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody updated: Customer): ResponseEntity<ApiResponse<Customer>> =
        customerService.update(id, updated)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = customerService.delete(id)

    @GetMapping("/search/by-name")
    fun getByNameIgnoreCase(@RequestParam name: String): ResponseEntity<ApiResponse<List<Customer>>> =
        customerService.getByNameIgnoreCase(name)

    @GetMapping("/search/by-visit-count")
    fun getByVisitCountBetween(
        @RequestParam min: Int,
        @RequestParam(defaultValue = Int.MAX_VALUE.toString()) max: Int = Int.MAX_VALUE
    ): ResponseEntity<ApiResponse<List<Customer>>> =
        customerService.getByVisitCountBetween(min, max)

    @GetMapping("/search/by-total-visit-count")
    fun getByTotalVisitCountBetween(
        @RequestParam min: Int,
        @RequestParam max: Int
    ): ResponseEntity<ApiResponse<List<Customer>>> =
        customerService.getByTotalVisitCountBetween(min, max)

    @GetMapping("/search/by-last-visit-date")
    fun getByLastVisitDateBetween(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) start: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) end: LocalDate
    ): ResponseEntity<ApiResponse<List<Customer>>> =
        customerService.getByLastVisitDateBetween(start, end)
}