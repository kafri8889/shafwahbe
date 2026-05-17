package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.enums.*
import com.anafthdev.shafwahbe.model.*
import com.anafthdev.shafwahbe.model.body.CustomerTransactionItemRequest
import com.anafthdev.shafwahbe.model.body.CustomerTransactionRequest
import com.anafthdev.shafwahbe.model.body.LegacyTransactionRequest
import com.anafthdev.shafwahbe.model.body.SimpleCustomerRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.PagedResponse
import com.anafthdev.shafwahbe.repository.*
import com.anafthdev.shafwahbe.repository.spec.CustomerTransactionSpecifications
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
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
    private val staffRepository: StaffRepository,
    private val treatmentCategoryRepository: TreatmentCategoryRepository,
    private val treatmentRepository: TreatmentRepository,
    private val treatmentPackageRepository: TreatmentPackageRepository,
    private val memberVoucherRepository: MemberVoucherRepository
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

        val employee = staffRepository.findByIdOrNull(request.employeeId)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Staff with ID ${request.employeeId} not found."))

        val amountBeforeVoucher = request.actualPrice.coerceAtLeast(0.0)
        val voucherResult = validateVoucherForTransaction(
            request.memberVoucherId,
            customer.id,
            amountBeforeVoucher,
            request.items
        )
        if (!voucherResult.success) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = voucherResult.message))
        }

        val voucherDiscountAmount = voucherResult.discountAmount
        val finalPrice = (amountBeforeVoucher - voucherDiscountAmount).coerceAtLeast(0.0)

        val newTransaction = CustomerTransaction(
            customer = customer,
            employee = employee,
            actualPrice = finalPrice,
            voucherDiscountAmount = voucherDiscountAmount,
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

            val itemEmployee = itemReq.employeeId?.let { staffId ->
                staffRepository.findByIdOrNull(staffId)
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse(success = false, message = "Staff with ID $staffId not found."))
            } ?: employee

            CustomerTransactionItem(
                treatment = treatment,
                treatmentPackage = treatmentPackage,
                employee = itemEmployee,
                treatmentType = itemReq.treatmentType,
                price = itemReq.price
            )
        }

        newTransaction.items.addAll(items)

        val saved = recordRepository.save(newTransaction)
        voucherResult.voucher?.let { voucher ->
            memberVoucherRepository.save(voucher.copy(
                status = MemberVoucherStatus.USED,
                usedTransaction = saved
            ))
        }

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
    fun createLegacyRecord(request: LegacyTransactionRequest): ResponseEntity<ApiResponse<CustomerTransaction>> {
        if (request.actualPrice <= 0.0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Legacy transaction amount must be greater than zero."))
        }

        if (request.commissionPercent !in setOf(5.0, 10.0, 15.0, 100.0)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Legacy commission must be 5, 10, 15, or 100 percent."))
        }

        val employee = staffRepository.findByIdOrNull(request.employeeId)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Staff with ID ${request.employeeId} not found."))
        val customer = resolveLegacyCustomer(request.date)
        val treatment = resolveLegacyTreatment(request.commissionPercent)
        val noteSuffix = "Input transaksi buku lama - komisi ${request.commissionPercent.toInt()}%"
        val notes = listOf(request.notes.trim(), noteSuffix).filter { it.isNotBlank() }.joinToString(" | ")

        val transaction = CustomerTransaction(
            customer = customer,
            employee = employee,
            actualPrice = request.actualPrice,
            voucherDiscountAmount = 0.0,
            paymentMethod = request.paymentMethod,
            notes = notes,
            date = request.date
        )

        transaction.items.add(
            CustomerTransactionItem(
                treatment = treatment,
                treatmentPackage = null,
                employee = employee,
                treatmentType = TreatmentType.Single,
                price = request.actualPrice
            )
        )

        val saved = recordRepository.save(transaction)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(success = true, message = "Legacy transaction created successfully.", data = saved))
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
            if (
                existingCustomer.name != customerReq.name ||
                existingCustomer.phoneNumber != trimmedPhone ||
                existingCustomer.birthDate != customerReq.birthDate
            ) {
                val updatedCustomer = existingCustomer.copy(
                    name = customerReq.name,
                    phoneNumber = trimmedPhone,
                    birthDate = customerReq.birthDate ?: existingCustomer.birthDate
                )
                customerRepository.save(updatedCustomer)
            } else existingCustomer

        val employee = staffRepository.findByIdOrNull(request.employeeId)
            ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse(success = false, message = "Staff with ID ${request.employeeId} not found."))

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

            val itemEmployee = itemReq.employeeId?.let { staffId ->
                staffRepository.findByIdOrNull(staffId)
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse(success = false, message = "Staff with ID $staffId not found."))
            } ?: employee

            CustomerTransactionItem(
                treatment = treatment,
                treatmentPackage = treatmentPackage,
                employee = itemEmployee,
                treatmentType = itemReq.treatmentType,
                price = itemReq.price
            )
        }

        existing.items.addAll(newItems)

        val updatedRecord = existing.copy(
            customer = customerToUse,
            employee = employee,
            actualPrice = request.actualPrice,
            voucherDiscountAmount = existing.voucherDiscountAmount,
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

    /**
     * Paginated, filterable transaction list.
     *
     * All filter params are optional; combinable via JPA Specification.
     *
     * @param startDate Inclusive lower bound on transaction `date` (date-only).
     * @param endDate Inclusive upper bound on transaction `date` (date-only).
     * @param customerId Restrict to a single customer.
     * @param employeeId Restrict to a single staff/stylist.
     * @param paymentMethod Restrict to a single payment method.
     * @param page 0-indexed page number.
     * @param size Page size; clamped to [1..200].
     * @param sort Comma-separated `field,direction`, defaults to `date,desc`.
     */
    fun getPagedRecords(
        startDate: LocalDate?,
        endDate: LocalDate?,
        customerId: Long?,
        employeeId: Long?,
        paymentMethod: PaymentMethod?,
        page: Int,
        size: Int,
        sort: String
    ): ResponseEntity<ApiResponse<PagedResponse<CustomerTransaction>>> {
        val safeSize = size.coerceIn(1, 200)
        val safePage = page.coerceAtLeast(0)
        val sortObj = parseSort(sort, defaultProperty = "date", defaultDirection = Sort.Direction.DESC)

        val spec: Specification<CustomerTransaction> =
            Specification.where(CustomerTransactionSpecifications.dateBetween(startDate, endDate))
                .and(CustomerTransactionSpecifications.customerId(customerId))
                .and(CustomerTransactionSpecifications.employeeId(employeeId))
                .and(CustomerTransactionSpecifications.paymentMethod(paymentMethod))

        val pageable = PageRequest.of(safePage, safeSize, sortObj)
        val result = recordRepository.findAll(spec, pageable)

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
            message = "Found ${result.totalElements} transactions.",
            data = body
        ))
    }

    private fun parseSort(raw: String, defaultProperty: String, defaultDirection: Sort.Direction): Sort {
        val parts = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return Sort.by(defaultDirection, defaultProperty)
        val property = parts[0]
        val direction = parts.getOrNull(1)?.let {
            if (it.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        } ?: defaultDirection
        return Sort.by(direction, property)
    }

    private fun resolveCustomerForCreate(req: SimpleCustomerRequest, txDate: LocalDateTime): Customer? {
        val phone = req.phoneNumber.trim()
        if (req.id != null) {
            val existing = customerRepository.findByIdOrNull(req.id)
            if (existing != null) {
                val updated = existing.copy(
                    name = req.name,
                    birthDate = req.birthDate ?: existing.birthDate,
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
                birthDate = req.birthDate,
                visitCount = 1,
                totalVisitCount = 1,
                lastVisitDate = txDate
            )
            return customerRepository.save(newCustomer)
        }
        return null
    }

    private fun resolveLegacyCustomer(txDate: LocalDateTime): Customer {
        val name = "Pelanggan Umum / Non Member"
        val existing = customerRepository.findFirstByNameIgnoreCase(name)
        if (existing != null) {
            return customerRepository.save(
                existing.copy(
                    lastVisitDate = txDate,
                    visitCount = existing.visitCount + 1,
                    totalVisitCount = existing.totalVisitCount + 1
                )
            )
        }

        return customerRepository.save(
            Customer(
                name = name,
                phoneNumber = "",
                address = "-",
                birthDate = null,
                visitCount = 1,
                totalVisitCount = 1,
                lastVisitDate = txDate
            )
        )
    }

    private fun resolveLegacyTreatment(commissionPercent: Double): Treatment {
        val title = "Transaksi Buku - Komisi ${commissionPercent.toInt()}%"
        treatmentRepository.findFirstByTitleIgnoreCase(title)?.let { return it }

        val category = treatmentCategoryRepository.findFirstByTitleIgnoreCase("Transaksi Buku")
            ?: treatmentCategoryRepository.save(
                TreatmentCategory(
                    title = "Transaksi Buku",
                    subTitle = "Input transaksi manual dari buku lama",
                    notes = listOf("Kategori sistem untuk transaksi legacy/manual.")
                )
            )

        return treatmentRepository.save(
            Treatment(
                category = category,
                title = title,
                active = true,
                priceType = PriceType.Fixed,
                prices = listOf(0.0),
                staffCommissionType = StaffCommissionType.PERCENTAGE,
                staffCommissionValue = commissionPercent
            )
        )
    }

    private data class VoucherValidationResult(
        val success: Boolean,
        val message: String,
        val voucher: MemberVoucher? = null,
        val discountAmount: Double = 0.0
    )

    private fun validateVoucherForTransaction(
        memberVoucherId: Long?,
        customerId: Long,
        subtotal: Double,
        items: List<CustomerTransactionItemRequest>
    ): VoucherValidationResult {
        if (memberVoucherId == null) {
            return VoucherValidationResult(success = true, message = "No voucher used.")
        }

        val voucher = memberVoucherRepository.findByIdOrNull(memberVoucherId)
            ?: return VoucherValidationResult(success = false, message = "Voucher with ID $memberVoucherId not found.")

        if (voucher.customer.id != customerId) {
            return VoucherValidationResult(success = false, message = "Voucher does not belong to selected customer.")
        }

        if (voucher.status != MemberVoucherStatus.ACTIVE) {
            return VoucherValidationResult(success = false, message = "Voucher is not active.")
        }

        if (voucher.effectiveValidityDays() >= 1 && voucher.expiresAt.isBefore(LocalDateTime.now())) {
            memberVoucherRepository.save(voucher.copy(status = MemberVoucherStatus.EXPIRED))
            return VoucherValidationResult(success = false, message = "Voucher is expired.")
        }

        if (!voucher.template.active) {
            return VoucherValidationResult(success = false, message = "Voucher template is inactive.")
        }

        if (subtotal < voucher.effectiveMinimumTransaction()) {
            return VoucherValidationResult(success = false, message = "Minimum transaction for voucher is not met.")
        }

        if (!voucher.effectiveAppliesToAll() && !voucherAppliesToItems(voucher, items)) {
            return VoucherValidationResult(success = false, message = "Voucher does not apply to selected treatment or package.")
        }

        val discountAmount = when (voucher.effectiveDiscountType()) {
            VoucherDiscountType.PERCENTAGE -> subtotal * voucher.effectiveDiscountValue().coerceIn(0.0, 100.0) / 100.0
            VoucherDiscountType.FIXED -> voucher.effectiveDiscountValue()
        }.coerceAtMost(subtotal)

        return VoucherValidationResult(
            success = true,
            message = "Voucher is valid.",
            voucher = voucher,
            discountAmount = discountAmount
        )
    }

    private fun voucherAppliesToItems(voucher: MemberVoucher, items: List<CustomerTransactionItemRequest>): Boolean {
        val treatmentIds = voucher.effectiveTreatmentIds()
        val packageIds = voucher.effectiveTreatmentPackageIds()

        return items.any { item ->
            (item.treatmentId != null && treatmentIds.contains(item.treatmentId)) ||
                (item.treatmentPackageId != null && packageIds.contains(item.treatmentPackageId))
        }
    }

    private fun MemberVoucher.hasSnapshot(): Boolean = !snapshotName.isNullOrBlank()

    private fun MemberVoucher.effectiveDiscountType(): VoucherDiscountType =
        if (hasSnapshot()) snapshotDiscountType ?: template.discountType else template.discountType

    private fun MemberVoucher.effectiveDiscountValue(): Double =
        if (hasSnapshot()) snapshotDiscountValue ?: template.discountValue else template.discountValue

    private fun MemberVoucher.effectiveMinimumTransaction(): Double =
        if (hasSnapshot()) snapshotMinimumTransaction ?: template.minimumTransaction else template.minimumTransaction

    private fun MemberVoucher.effectiveValidityDays(): Int =
        if (hasSnapshot()) snapshotValidityDays ?: template.validityDays else template.validityDays

    private fun MemberVoucher.effectiveAppliesToAll(): Boolean =
        if (hasSnapshot()) snapshotAppliesToAll ?: template.appliesToAll else template.appliesToAll

    private fun MemberVoucher.effectiveTreatmentIds(): Set<Long> =
        if (hasSnapshot()) snapshotTreatmentIds else template.treatmentIds

    private fun MemberVoucher.effectiveTreatmentPackageIds(): Set<Long> =
        if (hasSnapshot()) snapshotTreatmentPackageIds else template.treatmentPackageIds
}
