package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.enums.PaymentMethod
import com.anafthdev.shafwahbe.model.CustomerTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CustomerTransactionRepository : JpaRepository<CustomerTransaction, Long> {
     fun findByCustomerId(customerId: Long): List<CustomerTransaction>
     fun findByEmployeeIdAndDateBetween(employeeId: Long, startDate: LocalDate, endDate: LocalDate): List<CustomerTransaction>

    /**
     * 1. Mencari record berdasarkan rentang tanggal transaksi.
     */
    fun findByDateBetween(startDate: LocalDate, endDate: LocalDate): List<CustomerTransaction>

    /**
     * 2. Mencari record berdasarkan ID customer dan rentang tanggal transaksi.
     */
    fun findByCustomerIdAndDateBetween(customerId: Long, startDate: LocalDate, endDate: LocalDate): List<CustomerTransaction>

    /**
     * Mencari record berdasarkan ID employee saja (tanpa rentang tanggal).
     */
    fun findByEmployeeId(employeeId: Long): List<CustomerTransaction>

    /**
     * Mencari record berdasarkan metode pembayaran.
     */
    fun findByPaymentMethod(paymentMethod: PaymentMethod): List<CustomerTransaction>

    /**
     * Mencari record berdasarkan metode pembayaran dan rentang tanggal.
     */
    fun findByPaymentMethodAndDateBetween(paymentMethod: PaymentMethod, startDate: LocalDate, endDate: LocalDate): List<CustomerTransaction>

    /**
     * Mencari record berdasarkan beberapa kriteria sekaligus, contoh:
     * Customer, Employee, dan rentang tanggal. Ini bisa sangat spesifik.
     * Spring Data JPA mendukung kombinasi seperti ini.
     */
    fun findByCustomerIdAndEmployeeIdAndDateBetween(
        customerId: Long,
        employeeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CustomerTransaction>
}