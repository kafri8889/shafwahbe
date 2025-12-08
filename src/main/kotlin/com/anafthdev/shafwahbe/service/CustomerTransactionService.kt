package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.Customer
import com.anafthdev.shafwahbe.model.CustomerTransaction
import com.anafthdev.shafwahbe.model.CustomerTransactionItem
import com.anafthdev.shafwahbe.model.body.CustomerTransactionRequest
import com.anafthdev.shafwahbe.model.body.SimpleCustomerRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.CustomerRepository
import com.anafthdev.shafwahbe.repository.CustomerTransactionRepository
import com.anafthdev.shafwahbe.repository.EmployeeRepository
import com.anafthdev.shafwahbe.repository.TreatmentPackageRepository
import com.anafthdev.shafwahbe.repository.TreatmentRepository
import org.springframework.data.repository.findByIdOrNull // Ekstensi Kotlin
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CustomerTransactionService(
    private val recordRepository: CustomerTransactionRepository,
    private val customerRepository: CustomerRepository,
    private val employeeRepository: EmployeeRepository,
    private val treatmentRepository: TreatmentRepository,
    private val treatmentPackageRepository: TreatmentPackageRepository
) {

    fun getAllRecords(): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findAll()
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records.",
            data = records
        ))
    }

    fun getRecordById(id: Long): ResponseEntity<ApiResponse<CustomerTransaction>> {
        val record = recordRepository.findByIdOrNull(id)
        return if (record != null) {
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Record with ID $id found.",
                data = record
            ))
        } else {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Record with ID $id not found."
                ))
        }
    }

    @Transactional
    fun createRecord(request: CustomerTransactionRequest): ResponseEntity<ApiResponse<CustomerTransaction>> {
        val customer = resolveCustomerForCreate(request.customer, request.date)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Customer data is invalid (id / phoneNumber)."))

        val employee = employeeRepository.findByIdOrNull(request.employeeId)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Employee with ID ${request.employeeId} not found."))

        val newTransaction = CustomerTransaction(
            customer = customer,
            employee = employee,
            actualPrice = request.actualPrice,
            paymentMethod = request.paymentMethod,
            notes = request.notes,
            date = request.date
        )

        val items = request.items.map { itemReq ->
            val treatment = itemReq.treatmentId?.let { id ->
                treatmentRepository.findByIdOrNull(id)
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse(success = false, message = "Treatment with ID $id not found."))
            }

            val treatmentPackage = itemReq.treatmentPackageId?.let { id ->
                treatmentPackageRepository.findByIdOrNull(id)
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse(success = false, message = "Treatment Package with ID $id not found."))
            }

            CustomerTransactionItem(
                treatment = treatment,
                treatmentPackage = treatmentPackage,
                treatmentType = itemReq.treatmentType,
                price = itemReq.price
            )
        }

        newTransaction.items.addAll(items)

        val saved = recordRepository.save(newTransaction)

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                ApiResponse(
                    success = true,
                    message = "Transaction created successfully.",
                    data = saved
                )
            )
    }

    @Transactional
    fun updateRecord(id: Long, request: CustomerTransactionRequest): ResponseEntity<ApiResponse<CustomerTransaction>> {
        val existing = recordRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "Record with ID $id not found."))

        val customerReq = request.customer
        if (customerReq.id == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Customer id is required when updating a record."))
        }

        val existingCustomer = customerRepository.findByIdOrNull(customerReq.id)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Customer with ID ${customerReq.id} not found."))

        val trimmedPhone = customerReq.phoneNumber.trim()
        val customerToUse =
            if (existingCustomer.name != customerReq.name || existingCustomer.phoneNumber != trimmedPhone) {
                val updatedCustomer = existingCustomer.copy(
                    name = customerReq.name,
                    phoneNumber = trimmedPhone
                )
                customerRepository.save(updatedCustomer)
            } else existingCustomer

        val employee = employeeRepository.findByIdOrNull(request.employeeId)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Employee with ID ${request.employeeId} not found."))

        // hapus semua item lama (orphanRemoval bakal delete di DB)
        existing.items.clear()

        val newItems = request.items.map { itemReq ->
            val treatment = itemReq.treatmentId?.let { id ->
                treatmentRepository.findByIdOrNull(id)
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse(success = false, message = "Treatment with ID $id not found."))
            }

            val treatmentPackage = itemReq.treatmentPackageId?.let { id ->
                treatmentPackageRepository.findByIdOrNull(id)
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse(success = false, message = "Treatment Package with ID $id not found."))
            }

            CustomerTransactionItem(
                treatment = treatment,
                treatmentPackage = treatmentPackage,
                treatmentType = itemReq.treatmentType,
                price = itemReq.price
            )
        }

        existing.items.addAll(newItems)

        val updatedRecord = existing.copy(
            customer = customerToUse,
            employee = employee,
            actualPrice = request.actualPrice,
            paymentMethod = request.paymentMethod,
            notes = request.notes,
            date = request.date
        )

        val saved = recordRepository.save(updatedRecord)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Record updated successfully.",
                data = saved
            )
        )
    }


    fun deleteRecord(id: Long): ResponseEntity<ApiResponse<Unit>> {
        if (!recordRepository.existsById(id)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Record with ID $id not found."
                ))
        }
        recordRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Record with ID $id deleted successfully."
        ))
    }

    fun getRecordsByCustomerId(customerId: Long): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByCustomerId(customerId)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records for customer ID $customerId.",
            data = records
        ))
    }

    fun getRecordsByEmployeeIdAndDateBetween(employeeId: Long, startDate: LocalDate, endDate: LocalDate): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records for employee ID $employeeId between $startDate and $endDate.",
            data = records
        ))
    }

    fun getRecordsByDateBetween(startDate: LocalDate, endDate: LocalDate): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByDateBetween(startDate, endDate)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records between $startDate and $endDate.",
            data = records
        ))
    }

    fun getRecordsByCustomerIdAndDateBetween(customerId: Long, startDate: LocalDate, endDate: LocalDate): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByCustomerIdAndDateBetween(customerId, startDate, endDate)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records for customer ID $customerId between $startDate and $endDate.",
            data = records
        ))
    }

    fun getRecordsByEmployeeId(employeeId: Long): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByEmployeeId(employeeId)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records for employee ID $employeeId.",
            data = records
        ))
    }

    fun getRecordsByPaymentMethod(paymentMethod: PaymentMethod): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByPaymentMethod(paymentMethod)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records with payment method ${paymentMethod.name}.",
            data = records
        ))
    }

    fun getRecordsByPaymentMethodAndDateBetween(paymentMethod: PaymentMethod, startDate: LocalDate, endDate: LocalDate): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByPaymentMethodAndDateBetween(paymentMethod, startDate, endDate)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records with payment method ${paymentMethod.name} between $startDate and $endDate.",
            data = records
        ))
    }

    fun getRecordsByCustomerIdAndEmployeeIdAndDateBetween(customerId: Long, employeeId: Long, startDate: LocalDate, endDate: LocalDate): ResponseEntity<ApiResponse<List<CustomerTransaction>>> {
        val records = recordRepository.findByCustomerIdAndEmployeeIdAndDateBetween(customerId, employeeId, startDate, endDate)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${records.size} records for customer ID $customerId, employee ID $employeeId between $startDate and $endDate.",
            data = records
        ))
    }

    private fun resolveCustomerForCreate(req: SimpleCustomerRequest, txDate: LocalDateTime): Customer? {
        val phone = req.phoneNumber.trim()
        if (req.id != null) {
            val existing = customerRepository.findByIdOrNull(req.id)
            if (existing != null) {
                val updated = existing.copy(
                    name = req.name,
                    lastVisitDate = txDate,
                    visitCount = existing.visitCount + 1,
                    totalVisitCount = existing.totalVisitCount + 1
                )
                return customerRepository.save(updated)
            }
        }
        if (req.name.isNotEmpty()) {
            val newCustomer = Customer(
                name = req.name,
                phoneNumber = phone,
                address = "-",
                visitCount = 1,
                totalVisitCount = 1,
                lastVisitDate = txDate
            )
            return customerRepository.save(newCustomer)
        }
        return null
    }
}