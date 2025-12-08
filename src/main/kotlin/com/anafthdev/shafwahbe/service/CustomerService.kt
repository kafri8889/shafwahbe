package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.Customer
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.repository.CustomerRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class CustomerService(
    private val customerRepository: CustomerRepository
) {

    fun getAll(): ResponseEntity<ApiResponse<List<Customer>>> {
        val customers = customerRepository.findAll()
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${customers.size} customers.",
            data = customers
        ))
    }

    fun getById(id: Long): ResponseEntity<ApiResponse<Customer>> {
        val customer = customerRepository.findById(id)

        if (customer.isEmpty) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Customer with id $id does not exist!",
                ))
        }

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .body(ApiResponse(
                success = true,
                message = "Customer with ID $id found.",
                data = customer.get()
            ))
    }

    fun getByNameIgnoreCase(name: String): ResponseEntity<ApiResponse<List<Customer>>> {
        val customers = customerRepository.findByNameIgnoreCase(name)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${customers.size} customers with name (case insensitive) '$name'.",
            data = customers
        ))
    }

    fun getByVisitCountBetween(min: Int, max: Int): ResponseEntity<ApiResponse<List<Customer>>> {
        val customers = customerRepository.findByVisitCountBetween(min, max)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${customers.size} customers with visit count between $min and $max.",
            data = customers
        ))
    }

    fun getByTotalVisitCountBetween(min: Int, max: Int): ResponseEntity<ApiResponse<List<Customer>>> {
        val customers = customerRepository.findByTotalVisitCountBetween(min, max)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${customers.size} customers with total visit count between $min and $max.",
            data = customers
        ))
    }

    fun getByLastVisitDateBetween(start: LocalDate, end: LocalDate): ResponseEntity<ApiResponse<List<Customer>>> {
        val customers = customerRepository.findByLastVisitDateBetween(start, end)
        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${customers.size} customers with last visit date between $start and $end.",
            data = customers
        ))
    }

    fun create(customer: Customer): ResponseEntity<ApiResponse<Customer>> {
        customerRepository.save(customer)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Customer ${customer.name} created successfully!",
            data = customer
        ))
    }

    fun update(id: Long, updated: Customer): ResponseEntity<ApiResponse<Customer>> {
        val existing = getById(id)

        if (existing.statusCode == HttpStatus.NOT_FOUND) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = true,
                    message = "Customer with ID $id not found!"
                ))
        }

        val toUpdate = existing.body!!.data!!.copy(
            name = updated.name,
            phoneNumber = updated.phoneNumber,
            address = updated.address,
            visitCount = updated.visitCount,
            totalVisitCount = updated.totalVisitCount,
            lastVisitDate = updated.lastVisitDate
        )

        customerRepository.save(toUpdate)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Customer ${toUpdate.name} updated!",
            data = toUpdate
        ))
    }

    fun delete(id: Long): ResponseEntity<ApiResponse<Customer>> {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(
                    success = false,
                    message = "Customer with ID $id not found."
                ))
        }

        customerRepository.deleteById(id)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Customer with ID $id deleted!"
        ))
    }

}