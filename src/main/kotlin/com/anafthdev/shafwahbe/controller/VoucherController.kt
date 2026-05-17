package com.anafthdev.shafwahbe.controller

import com.anafthdev.shafwahbe.model.body.AssignVoucherRequest
import com.anafthdev.shafwahbe.model.body.VoucherTemplateRequest
import com.anafthdev.shafwahbe.service.VoucherService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/vouchers")
class VoucherController(
    private val voucherService: VoucherService
) {

    @GetMapping("/templates")
    fun getTemplates() = voucherService.getTemplates()

    @PostMapping("/templates")
    fun createTemplate(@RequestBody request: VoucherTemplateRequest) = voucherService.createTemplate(request)

    @PutMapping("/templates/{id}")
    fun updateTemplate(@PathVariable id: Long, @RequestBody request: VoucherTemplateRequest) =
        voucherService.updateTemplate(id, request)

    @PatchMapping("/templates/{id}/deactivate")
    fun deactivateTemplate(@PathVariable id: Long) = voucherService.deactivateTemplate(id)

    @PostMapping("/assign")
    fun assignVoucher(@RequestBody request: AssignVoucherRequest) = voucherService.assignVoucher(request)

    @GetMapping("/member/{customerId}")
    fun getMemberVouchers(@PathVariable customerId: Long) = voucherService.getMemberVouchers(customerId)

    @GetMapping("/member-vouchers")
    fun getAllMemberVouchers() = voucherService.getAllMemberVouchers()

    @PatchMapping("/member-vouchers/{id}/cancel")
    fun cancelMemberVoucher(@PathVariable id: Long) = voucherService.cancelMemberVoucher(id)
}
