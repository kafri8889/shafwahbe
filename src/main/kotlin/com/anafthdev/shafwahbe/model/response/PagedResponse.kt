package com.anafthdev.shafwahbe.model.response

/**
 * Generic paginated response wrapper.
 *
 * Wrapped inside [ApiResponse] when an endpoint returns a paginated list.
 */
data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)
