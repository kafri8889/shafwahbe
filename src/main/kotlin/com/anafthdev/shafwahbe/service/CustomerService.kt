package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.model.Customer
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.PagedResponse
import com.anafthdev.shafwahbe.repository.CustomerRepository
import com.anafthdev.shafwahbe.repository.spec.CustomerSpecifications
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
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

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Customer with ID $id found.",
            data = customer.get()
        ))
    }

    /**
     * Paginated, filterable customer list.
     *
     * @param search Free-text keyword applied to name, phone number, address, and birth date (case-insensitive LIKE).
     * @param startDate Inclusive lower bound on `lastVisitDate` (date-only).
     * @param endDate Inclusive upper bound on `lastVisitDate` (date-only).
     * @param page 0-indexed page number.
     * @param size Page size; clamped to [1..200].
     * @param sort Comma-separated `field,direction` pair, e.g. `lastVisitDate,desc`.
     */
    fun getPaged(
        search: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        page: Int,
        size: Int,
        sort: String
    ): ResponseEntity<ApiResponse<PagedResponse<Customer>>> {
        val safeSize = size.coerceIn(1, 200)
        val safePage = page.coerceAtLeast(0)
        val sortObj = parseSort(sort, defaultProperty = "lastVisitDate", defaultDirection = Sort.Direction.DESC)

        val spec: Specification<Customer> = Specification.where(CustomerSpecifications.search(search))
            .and(CustomerSpecifications.lastVisitDateBetween(startDate, endDate))

        val pageable = PageRequest.of(safePage, safeSize, sortObj)
        val result = customerRepository.findAll(spec, pageable)

        val body = PagedResponse(
            content = result.content,
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            first = result.isFirst,
            last = result.isLast
        )

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${result.totalElements} customers.",
            data = body
        ))
    }

    fun create(customer: Customer): ResponseEntity<ApiResponse<Customer>> {
        val newCustomer = customer.copy(
            id = 0,
            visitCount = 0,
            totalVisitCount = 0
        )
        val saved = customerRepository.save(newCustomer)

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Customer ${saved.name} created successfully!",
            data = saved
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
            birthDate = updated.birthDate,
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

    /**
     * Accepts strings like "lastVisitDate,desc", "name,asc", or just "name".
     * Falls back to the supplied defaults when input is empty or malformed.
     */
    private fun parseSort(raw: String, defaultProperty: String, defaultDirection: Sort.Direction): Sort {
        val parts = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return Sort.by(defaultDirection, defaultProperty)
        val property = parts[0]
        val direction = parts.getOrNull(1)?.let {
            if (it.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        } ?: defaultDirection
        return Sort.by(direction, property)
    }
}
