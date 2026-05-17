package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.CustomerTransaction
import com.anafthdev.shafwahbe.model.body.CustomerTransactionRequest
import com.anafthdev.shafwahbe.model.body.LegacyTransactionRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
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

    // --- Endpoint Pencarian ---

    @GetMapping("/search/by-customer")
    fun getRecordsByCustomerId(@RequestParam customerId: Long): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByCustomerId(customerId)

    @GetMapping("/search/by-employee-and-date")
    fun getRecordsByEmployeeIdAndDateBetween(
        @RequestParam employeeId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByEmployeeIdAndDateBetween(employeeId, startDate, endDate)

    @GetMapping("/search/by-date")
    fun getRecordsByDateBetween(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByDateBetween(startDate, endDate)

    @GetMapping("/search/by-customer-and-date")
    fun getRecordsByCustomerIdAndDateBetween(
        @RequestParam customerId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByCustomerIdAndDateBetween(customerId, startDate, endDate)

    @GetMapping("/search/by-employee")
    fun getRecordsByEmployeeId(@RequestParam employeeId: Long): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByEmployeeId(employeeId)

    @GetMapping("/search/by-payment-method")
    fun getRecordsByPaymentMethod(@RequestParam paymentMethod: PaymentMethod): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByPaymentMethod(paymentMethod)

    @GetMapping("/search/by-payment-method-and-date")
    fun getRecordsByPaymentMethodAndDateBetween(
        @RequestParam paymentMethod: PaymentMethod,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByPaymentMethodAndDateBetween(paymentMethod, startDate, endDate)

    @GetMapping("/search/by-customer-employee-date")
    fun getRecordsByCustomerIdAndEmployeeIdAndDateBetween(
        @RequestParam customerId: Long,
        @RequestParam employeeId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ApiResponse<List<CustomerTransaction>>> =
        recordService.getRecordsByCustomerIdAndEmployeeIdAndDateBetween(customerId, employeeId, startDate, endDate)
}
