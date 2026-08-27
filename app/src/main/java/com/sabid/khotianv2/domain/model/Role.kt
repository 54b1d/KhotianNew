package com.sabid.khotianv2.domain.model

data class Role(
    val id: Long = 0,
    val name: String,
    val permissions: List<PermissionType> = emptyList()
)
