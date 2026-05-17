package com.anafthdev.shafwahbe.repository

import com.anafthdev.shafwahbe.model.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository :
    JpaRepository<Customer, Long>,
    JpaSpecificationExecutor<Customer> {

    /**
     * Used internally by [com.anafthdev.shafwahbe.service.CustomerTransactionService.resolveLegacyCustomer]
     * to look up the shared "Pelanggan Umum / Non Member" record.
     */
    fun findFirstByNameIgnoreCase(name: String): Customer?
}
