package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.CustomerTransaction
import com.anafthdev.shafwahbe.model.body.CustomerTransactionRequest
import com.anafthdev.shafwahbe.model.body.LegacyTransactionRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.PagedResponse
import com.anafthdev.shafwahbe.service.CustomerTransactionService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/transaction")
class CustomerTransactionController(
    private val recordService: CustomerTransactionService
) {

    @GetMapping
    fun getAllRecords(): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getAllRecords()

    @GetMapping("/paged")
    fun getPagedRecords(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @RequestParam(required = false) customerId: Long?,
        @RequestParam(required = false) employeeId: Long?,
        @RequestParam(required = false) paymentMethod: PaymentMethod?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "date,desc") sort: String
    ): ResponseEntity<ApiResponse<PagedResponse<CustomerTransaction>>> =
        recordService.getPagedRecords(startDate, endDate, customerId, employeeId, paymentMethod, page, size, sort)

    @GetMapping("/{id}")
    fun getRecordById(@PathVariable id: Long): ResponseEntity<ApiResponse<CustomerTransaction>> =
        recordService.getRecordById(id)

    @PostMapping
    fun createRecord(@RequestBody request: CustomerTransactionRequest): ResponseEntity<ApiResponse<CustomerTransaction>> =
        recordService.createRecord(request)

    @PostMapping("/legacy")
    fun createLegacyRecord(@RequestBody request: LegacyTransactionRequest): ResponseEntity<ApiResponse<CustomerTransaction>> =
        recordService.createLegacyRecord(request)

    @PutMapping("/{id}")
    fun updateRecord(@PathVariable id: Long, @RequestBody request: CustomerTransactionRequest): ResponseEntity<ApiResponse<CustomerTransaction>> =
        recordService.updateRecord(id, request)

    @DeleteMapping("/{id}")
    fun deleteRecord(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> =
        recordService.deleteRecord(id)
}
