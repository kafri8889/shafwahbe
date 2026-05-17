package com.anafthdev.shafwahbe.repository.spec

import com.anafthdev.shafwahbe.model.Customer
import com.anafthdev.shafwahbe.repository.spec.CustomerSpecifications.search
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Reusable JPA specifications for [Customer] queries.
 *
 * The [search] helper performs a case-insensitive LIKE across name,
 * phone number, address, and birth date (rendered as ISO string).
 */
object CustomerSpecifications {

    fun search(keyword: String?): Specification<Customer>? {
        val trimmed = keyword?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        val pattern = "%${trimmed.lowercase()}%"
        return Specification { root, _, cb ->
            val name = cb.lower(root.get<String>("name"))
            val phone = cb.lower(root.get<String>("phoneNumber"))
            val address = cb.lower(root.get<String>("address"))
            val birthDate = cb.lower(cb.coalesce(root.get<LocalDate>("birthDate").`as`(String::class.java), ""))
            cb.or(
                cb.like(name, pattern),
                cb.like(phone, pattern),
                cb.like(address, pattern),
                cb.like(birthDate, pattern)
            )
        }
    }

    fun lastVisitDateBetween(startDate: LocalDate?, endDate: LocalDate?): Specification<Customer>? {
        if (startDate == null && endDate == null) return null
        return Specification { root, _, cb ->
            val datePath = root.get<LocalDateTime>("lastVisitDate")
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
}
