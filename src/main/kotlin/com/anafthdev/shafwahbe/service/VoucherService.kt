package com.anafthdev.shafwahbe.service

import com.anafthdev.shafwahbe.enums.MemberVoucherStatus
import com.anafthdev.shafwahbe.model.MemberVoucher
import com.anafthdev.shafwahbe.model.VoucherTemplate
import com.anafthdev.shafwahbe.model.body.AssignVoucherRequest
import com.anafthdev.shafwahbe.model.body.VoucherTemplateRequest
import com.anafthdev.shafwahbe.model.response.ApiResponse
import com.anafthdev.shafwahbe.model.response.MemberVoucherResponse
import com.anafthdev.shafwahbe.model.response.VoucherTemplateResponse
import com.anafthdev.shafwahbe.model.response.toMemberVoucherResponse
import com.anafthdev.shafwahbe.model.response.toVoucherTemplateResponse
import com.anafthdev.shafwahbe.repository.CustomerRepository
import com.anafthdev.shafwahbe.repository.MemberVoucherRepository
import com.anafthdev.shafwahbe.repository.VoucherTemplateRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class VoucherService(
    private val voucherTemplateRepository: VoucherTemplateRepository,
    private val memberVoucherRepository: MemberVoucherRepository,
    private val customerRepository: CustomerRepository
) {
    private val noExpiryDate: LocalDateTime = LocalDateTime.of(9999, 12, 31, 23, 59, 59)

    fun getTemplates(): ResponseEntity<ApiResponse<List<VoucherTemplateResponse>>> {
        val templates = voucherTemplateRepository.findAll()
            .sortedByDescending { it.createdAt }
            .map { it.toVoucherTemplateResponse() }

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${templates.size} voucher templates.",
            data = templates
        ))
    }

    fun createTemplate(request: VoucherTemplateRequest): ResponseEntity<ApiResponse<VoucherTemplateResponse>> {
        val validationError = validateTemplateRequest(request)
        if (validationError != null) {
            return ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = validationError
            ))
        }

        val saved = voucherTemplateRepository.save(
            VoucherTemplate(
                name = request.name.trim(),
                description = request.description.trim(),
                discountType = request.discountType,
                discountValue = request.discountValue,
                minimumTransaction = request.minimumTransaction,
                validityDays = request.validityDays,
                appliesToAll = request.appliesToAll,
                treatmentIds = scopedTreatmentIds(request),
                treatmentPackageIds = scopedTreatmentPackageIds(request),
                active = request.active
            )
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse(
            success = true,
            message = "Voucher template '${saved.name}' created.",
            data = saved.toVoucherTemplateResponse()
        ))
    }

    fun updateTemplate(id: Long, request: VoucherTemplateRequest): ResponseEntity<ApiResponse<VoucherTemplateResponse>> {
        val existing = voucherTemplateRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse(
                success = false,
                message = "Voucher template with ID $id not found."
            ))

        val validationError = validateTemplateRequest(request)
        if (validationError != null) {
            return ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = validationError
            ))
        }

        val saved = voucherTemplateRepository.save(existing.copy(
            name = request.name.trim(),
            description = request.description.trim(),
            discountType = request.discountType,
            discountValue = request.discountValue,
            minimumTransaction = request.minimumTransaction,
            validityDays = request.validityDays,
            appliesToAll = request.appliesToAll,
            treatmentIds = scopedTreatmentIds(request),
            treatmentPackageIds = scopedTreatmentPackageIds(request),
            active = request.active
        ))

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Voucher template '${saved.name}' updated.",
            data = saved.toVoucherTemplateResponse()
        ))
    }

    fun deactivateTemplate(id: Long): ResponseEntity<ApiResponse<VoucherTemplateResponse>> {
        val existing = voucherTemplateRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse(
                success = false,
                message = "Voucher template with ID $id not found."
            ))

        val saved = voucherTemplateRepository.save(existing.copy(active = false))

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Voucher template '${saved.name}' deactivated.",
            data = saved.toVoucherTemplateResponse()
        ))
    }

    @Transactional
    fun assignVoucher(request: AssignVoucherRequest): ResponseEntity<ApiResponse<MemberVoucherResponse>> {
        val template = voucherTemplateRepository.findByIdOrNull(request.templateId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse(
                success = false,
                message = "Voucher template with ID ${request.templateId} not found."
            ))

        if (!template.active) {
            return ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Voucher template is not active."
            ))
        }

        val customer = customerRepository.findByIdOrNull(request.customerId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse(
                success = false,
                message = "Customer with ID ${request.customerId} not found."
            ))

        val now = LocalDateTime.now()
        val expiresAt = if (template.validityDays < 1) noExpiryDate else now.plusDays(template.validityDays.toLong())
        val saved = memberVoucherRepository.save(
            MemberVoucher(
                template = template,
                customer = customer,
                code = generateUniqueCode(),
                assignedAt = now,
                expiresAt = expiresAt,
                snapshotName = template.name,
                snapshotDescription = template.description,
                snapshotDiscountType = template.discountType,
                snapshotDiscountValue = template.discountValue,
                snapshotMinimumTransaction = template.minimumTransaction,
                snapshotValidityDays = template.validityDays,
                snapshotAppliesToAll = template.appliesToAll,
                snapshotTreatmentIds = template.treatmentIds,
                snapshotTreatmentPackageIds = template.treatmentPackageIds,
                status = MemberVoucherStatus.ACTIVE
            )
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse(
            success = true,
            message = "Voucher assigned to ${customer.name}.",
            data = saved.toMemberVoucherResponse()
        ))
    }

    fun getMemberVouchers(customerId: Long): ResponseEntity<ApiResponse<List<MemberVoucherResponse>>> {
        if (!customerRepository.existsById(customerId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse(
                success = false,
                message = "Customer with ID $customerId not found."
            ))
        }

        val vouchers = memberVoucherRepository.findByCustomerIdOrderByExpiresAtDesc(customerId)
            .map { normalizeStatus(it).toMemberVoucherResponse() }

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${vouchers.size} vouchers for customer ID $customerId.",
            data = vouchers
        ))
    }

    fun getAllMemberVouchers(): ResponseEntity<ApiResponse<List<MemberVoucherResponse>>> {
        val vouchers = memberVoucherRepository.findAll()
            .sortedByDescending { it.assignedAt }
            .map { normalizeStatus(it).toMemberVoucherResponse() }

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Found ${vouchers.size} member vouchers.",
            data = vouchers
        ))
    }

    fun cancelMemberVoucher(id: Long): ResponseEntity<ApiResponse<MemberVoucherResponse>> {
        val existing = memberVoucherRepository.findByIdOrNull(id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse(
                success = false,
                message = "Member voucher with ID $id not found."
            ))

        if (existing.status == MemberVoucherStatus.USED) {
            return ResponseEntity.badRequest().body(ApiResponse(
                success = false,
                message = "Used voucher cannot be cancelled."
            ))
        }

        val saved = memberVoucherRepository.save(existing.copy(status = MemberVoucherStatus.CANCELLED))

        return ResponseEntity.ok(ApiResponse(
            success = true,
            message = "Voucher ${saved.code} cancelled.",
            data = saved.toMemberVoucherResponse()
        ))
    }

    private fun normalizeStatus(voucher: MemberVoucher): MemberVoucher {
        if (
            voucher.status == MemberVoucherStatus.ACTIVE &&
            voucher.effectiveValidityDays() >= 1 &&
            voucher.expiresAt.isBefore(LocalDateTime.now())
        ) {
            return memberVoucherRepository.save(voucher.copy(status = MemberVoucherStatus.EXPIRED))
        }

        return voucher
    }

    private fun validateTemplateRequest(request: VoucherTemplateRequest): String? {
        if (request.name.isBlank()) return "Voucher name is required."
        if (request.discountValue <= 0.0) return "Discount value must be greater than 0."
        if (request.minimumTransaction < 0.0) return "Minimum transaction cannot be negative."
        if (!request.appliesToAll && request.treatmentIds.isEmpty() && request.treatmentPackageIds.isEmpty()) {
            return "Select at least one treatment or package, or set voucher to apply to all."
        }
        return null
    }

    private fun scopedTreatmentIds(request: VoucherTemplateRequest): Set<Long> =
        if (request.appliesToAll) emptySet() else request.treatmentIds.filter { it > 0 }.toSet()

    private fun scopedTreatmentPackageIds(request: VoucherTemplateRequest): Set<Long> =
        if (request.appliesToAll) emptySet() else request.treatmentPackageIds.filter { it > 0 }.toSet()

    private fun MemberVoucher.effectiveValidityDays(): Int =
        if (!snapshotName.isNullOrBlank()) snapshotValidityDays ?: template.validityDays else template.validityDays

    private fun generateUniqueCode(): String {
        repeat(12) {
            val code = "SHF-${UUID.randomUUID().toString().take(8).uppercase()}"
            if (!memberVoucherRepository.existsByCode(code)) return code
        }

        return "SHF-${System.currentTimeMillis()}"
    }
}
