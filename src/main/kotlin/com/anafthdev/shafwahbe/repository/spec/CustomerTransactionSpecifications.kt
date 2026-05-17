package com.anafthdev.shafwahbe.repository.spec

import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.CustomerTransaction
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Reusable JPA specifications for [CustomerTransaction] queries.
 *
 * Each specification narrows the result set; combine them via
 * [Specification.where] / [Specification.and] in the service layer.
 */
object CustomerTransactionSpecifications {

    fun dateBetween(startDate: LocalDate?, endDate: LocalDate?): Specification<CustomerTransaction>? {
        if (startDate == null && endDate == null) return null
        return Specification { root, _, cb ->
            // Compare against LocalDateTime field with day-precision boundaries.
            val datePath = root.get<LocalDateTime>("date")
            when {
                startDate != null && endDate != null -> {
                    val from = startDate.atStartOfDay()
                    val to = endDate.atTime(LocalTime.MAX)
                    cb.between(datePath, from, to)
                }
                startDate != null -> cb.greaterThanOrEqualTo(datePath, startDate.atStartOfDay())
                else -> cb.lessThanOrEqualTo(datePath, endDate!!.atTime(LocalTime.MAX))
            }
        }
    }

    fun customerId(customerId: Long?): Specification<CustomerTransaction>? {
        if (customerId == null) return null
        return Specification { root, _, cb ->
            cb.equal(root.get<Any>("customer").get<Long>("id"), customerId)
        }
    }

    fun employeeId(employeeId: Long?): Specification<CustomerTransaction>? {
        if (employeeId == null) return null
        return Specification { root, _, cb ->
            cb.equal(root.get<Any>("employee").get<Long>("id"), employeeId)
        }
    }

    fun paymentMethod(paymentMethod: PaymentMethod?): Specification<CustomerTransaction>? {
        if (paymentMethod == null) return null
        return Specification { root, _, cb ->
            cb.equal(root.get<PaymentMethod>("paymentMethod"), paymentMethod)
        }
    }
}
