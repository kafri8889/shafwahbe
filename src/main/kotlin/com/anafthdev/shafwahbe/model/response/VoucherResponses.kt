package com.anafthdev.shafwahbe.model.response

import com.anafthdev.shafwahbe.enums.MemberVoucherStatus
import com.anafthdev.shafwahbe.enums.VoucherDiscountType
import com.anafthdev.shafwahbe.model.MemberVoucher
import com.anafthdev.shafwahbe.model.VoucherTemplate
import java.time.LocalDateTime

data class VoucherTemplateResponse(
    val id: Long,
    val name: String,
    val description: String,
    val discountType: VoucherDiscountType,
    val discountValue: Double,
    val minimumTransaction: Double,
    val validityDays: Int,
    val neverExpires: Boolean,
    val appliesToAll: Boolean,
    val treatmentIds: Set<Long>,
    val treatmentPackageIds: Set<Long>,
    val active: Boolean,
    val createdAt: LocalDateTime
)

data class MemberVoucherResponse(
    val id: Long,
    val template: VoucherTemplateResponse,
    val customerId: Long,
    val customerName: String,
    val code: String,
    val assignedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val neverExpires: Boolean,
    val status: MemberVoucherStatus,
    val usedTransactionId: Long?
)

fun VoucherTemplate.toVoucherTemplateResponse(): VoucherTemplateResponse = VoucherTemplateResponse(
    id = id,
    name = name,
    description = description,
    discountType = discountType,
    discountValue = discountValue,
    minimumTransaction = minimumTransaction,
    validityDays = validityDays,
    neverExpires = validityDays < 1,
    appliesToAll = appliesToAll,
    treatmentIds = treatmentIds,
    treatmentPackageIds = treatmentPackageIds,
    active = active,
    createdAt = createdAt
)

fun MemberVoucher.toSnapshotVoucherTemplateResponse(): VoucherTemplateResponse {
    val hasSnapshot = !snapshotName.isNullOrBlank()

    return VoucherTemplateResponse(
        id = template.id,
        name = if (hasSnapshot) snapshotName ?: template.name else template.name,
        description = if (hasSnapshot) snapshotDescription ?: "" else template.description,
        discountType = if (hasSnapshot) snapshotDiscountType ?: template.discountType else template.discountType,
        discountValue = if (hasSnapshot) snapshotDiscountValue ?: template.discountValue else template.discountValue,
        minimumTransaction = if (hasSnapshot) snapshotMinimumTransaction ?: template.minimumTransaction else template.minimumTransaction,
        validityDays = if (hasSnapshot) snapshotValidityDays ?: template.validityDays else template.validityDays,
        neverExpires = if (hasSnapshot) (snapshotValidityDays ?: template.validityDays) < 1 else template.validityDays < 1,
        appliesToAll = if (hasSnapshot) snapshotAppliesToAll ?: template.appliesToAll else template.appliesToAll,
        treatmentIds = if (hasSnapshot) snapshotTreatmentIds else template.treatmentIds,
        treatmentPackageIds = if (hasSnapshot) snapshotTreatmentPackageIds else template.treatmentPackageIds,
        active = template.active,
        createdAt = template.createdAt
    )
}

fun MemberVoucher.toMemberVoucherResponse(): MemberVoucherResponse = MemberVoucherResponse(
    id = id,
    template = toSnapshotVoucherTemplateResponse(),
    customerId = customer.id,
    customerName = customer.name,
    code = code,
    assignedAt = assignedAt,
    expiresAt = expiresAt,
    neverExpires = toSnapshotVoucherTemplateResponse().neverExpires,
    status = status,
    usedTransactionId = usedTransaction?.id
)
