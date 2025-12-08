package com.anafthdev.shafwahbe.model.response

import java.time.LocalDateTime

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val data: T? = null
)
