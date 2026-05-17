package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.Customer
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.PagedResponse
import com.anafthdev.shafwahbe.service.CustomerService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/customers")
class CustomerController(
    private val customerService: CustomerService
) {

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse<List<Customer>>> = customerService.getAll()

    @GetMapping("/paged")
    fun getPaged(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "lastVisitDate,desc") sort: String
    ): ResponseEntity<ApiResponse<PagedResponse<Customer>>> =
        customerService.getPaged(search, startDate, endDate, page, size, sort)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<ApiResponse<Customer>> = customerService.getById(id)

    @PostMapping
    fun create(@RequestBody customer: Customer): ResponseEntity<ApiResponse<Customer>> = customerService.create(customer)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody updated: Customer): ResponseEntity<ApiResponse<Customer>> =
        customerService.update(id, updated)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = customerService.delete(id)
}
