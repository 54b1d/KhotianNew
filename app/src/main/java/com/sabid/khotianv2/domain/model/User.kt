package com.sabid.khotianv2.domain.model

data class User(
    val id: String,
    val username: String,
    val roleId: Long?,
    val roleName: String?,
    val isActive: Boolean
)
