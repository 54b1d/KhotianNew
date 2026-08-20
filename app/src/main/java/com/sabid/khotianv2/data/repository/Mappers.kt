package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.data.local.entity.*
import com.sabid.khotianv2.domain.model.*

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    partyId = partyId,
    productId = productId,
    unitId = unitId,
    quantity = quantity,
    baseQuantity = baseQuantity,
    rate = rate,
    amount = amount,
    freightAmount = freightAmount,
    freightType = freightType.toDomain(),
    netCost = netCost,
    financialAccountId = financialAccountId,
    toFinancialAccountId = toFinancialAccountId,
    type = type.toDomain(),
    businessType = businessType.toDomain(),
    note = note,
    timestamp = timestamp,
    createdBy = createdBy
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    partyId = partyId,
    productId = productId,
    unitId = unitId,
    quantity = quantity,
    baseQuantity = baseQuantity,
    rate = rate,
    amount = amount,
    freightAmount = freightAmount,
    freightType = freightType.toEntity(),
    netCost = netCost,
    financialAccountId = financialAccountId,
    toFinancialAccountId = toFinancialAccountId,
    type = type.toEntity(),
    businessType = businessType.toEntity(),
    note = note,
    timestamp = timestamp,
    createdBy = createdBy
)

fun PartyEntity.toDomain() = Party(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    address = address,
    type = type,
    openingBalance = openingBalance
)

fun Party.toEntity() = PartyEntity(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    address = address,
    type = type,
    openingBalance = openingBalance
)

fun ProductEntity.toDomain() = Product(
    id = id,
    name = name,
    defaultUnitId = defaultUnitId,
    category = category,
    openingBalance = openingBalance
)

fun Product.toEntity() = ProductEntity(
    id = id,
    name = name,
    defaultUnitId = defaultUnitId,
    category = category,
    openingBalance = openingBalance
)

fun UnitEntity.toDomain() = AppUnit(
    id = id,
    name = name,
    symbol = symbol,
    multiplier = multiplier
)

fun AppUnit.toEntity() = UnitEntity(
    id = id,
    name = name,
    symbol = symbol,
    multiplier = multiplier
)

fun com.sabid.khotianv2.data.local.entity.FreightType.toDomain() = when (this) {
    com.sabid.khotianv2.data.local.entity.FreightType.BORN_BY_US -> com.sabid.khotianv2.domain.model.FreightType.BORN_BY_US
    com.sabid.khotianv2.data.local.entity.FreightType.BORN_BY_SELLER -> com.sabid.khotianv2.domain.model.FreightType.BORN_BY_SELLER
}

fun com.sabid.khotianv2.domain.model.FreightType.toEntity() = when (this) {
    com.sabid.khotianv2.domain.model.FreightType.BORN_BY_US -> com.sabid.khotianv2.data.local.entity.FreightType.BORN_BY_US
    com.sabid.khotianv2.domain.model.FreightType.BORN_BY_SELLER -> com.sabid.khotianv2.data.local.entity.FreightType.BORN_BY_SELLER
}

fun com.sabid.khotianv2.data.local.entity.TransactionType.toDomain() = when (this) {
    com.sabid.khotianv2.data.local.entity.TransactionType.DEBIT -> com.sabid.khotianv2.domain.model.TransactionType.DEBIT
    com.sabid.khotianv2.data.local.entity.TransactionType.CREDIT -> com.sabid.khotianv2.domain.model.TransactionType.CREDIT
    com.sabid.khotianv2.data.local.entity.TransactionType.TRANSFER -> com.sabid.khotianv2.domain.model.TransactionType.TRANSFER
}

fun com.sabid.khotianv2.domain.model.TransactionType.toEntity() = when (this) {
    com.sabid.khotianv2.domain.model.TransactionType.DEBIT -> com.sabid.khotianv2.data.local.entity.TransactionType.DEBIT
    com.sabid.khotianv2.domain.model.TransactionType.CREDIT -> com.sabid.khotianv2.data.local.entity.TransactionType.CREDIT
    com.sabid.khotianv2.domain.model.TransactionType.TRANSFER -> com.sabid.khotianv2.data.local.entity.TransactionType.TRANSFER
}

fun com.sabid.khotianv2.data.local.entity.BusinessTransactionType.toDomain() = when (this) {
    com.sabid.khotianv2.data.local.entity.BusinessTransactionType.PURCHASE -> com.sabid.khotianv2.domain.model.BusinessTransactionType.PURCHASE
    com.sabid.khotianv2.data.local.entity.BusinessTransactionType.SALE -> com.sabid.khotianv2.domain.model.BusinessTransactionType.SALE
    com.sabid.khotianv2.data.local.entity.BusinessTransactionType.PAYMENT_MADE -> com.sabid.khotianv2.domain.model.BusinessTransactionType.PAYMENT_MADE
    com.sabid.khotianv2.data.local.entity.BusinessTransactionType.PAYMENT_RECEIVED -> com.sabid.khotianv2.domain.model.BusinessTransactionType.PAYMENT_RECEIVED
    com.sabid.khotianv2.data.local.entity.BusinessTransactionType.TRANSFER -> com.sabid.khotianv2.domain.model.BusinessTransactionType.TRANSFER
}

fun com.sabid.khotianv2.domain.model.BusinessTransactionType.toEntity() = when (this) {
    com.sabid.khotianv2.domain.model.BusinessTransactionType.PURCHASE -> com.sabid.khotianv2.data.local.entity.BusinessTransactionType.PURCHASE
    com.sabid.khotianv2.domain.model.BusinessTransactionType.SALE -> com.sabid.khotianv2.data.local.entity.BusinessTransactionType.SALE
    com.sabid.khotianv2.domain.model.BusinessTransactionType.PAYMENT_MADE -> com.sabid.khotianv2.data.local.entity.BusinessTransactionType.PAYMENT_MADE
    com.sabid.khotianv2.domain.model.BusinessTransactionType.PAYMENT_RECEIVED -> com.sabid.khotianv2.data.local.entity.BusinessTransactionType.PAYMENT_RECEIVED
    com.sabid.khotianv2.domain.model.BusinessTransactionType.TRANSFER -> com.sabid.khotianv2.data.local.entity.BusinessTransactionType.TRANSFER
}

fun FinancialAccountEntity.toDomain() = FinancialAccount(
    id = id,
    name = name,
    type = type.toDomain(),
    openingBalance = openingBalance,
    currentBalance = currentBalance
)

fun FinancialAccount.toEntity() = FinancialAccountEntity(
    id = id,
    name = name,
    type = type.toEntity(),
    openingBalance = openingBalance,
    currentBalance = currentBalance
)

fun com.sabid.khotianv2.data.local.entity.FinancialAccountType.toDomain() = when (this) {
    com.sabid.khotianv2.data.local.entity.FinancialAccountType.CASH -> com.sabid.khotianv2.domain.model.FinancialAccountType.CASH
    com.sabid.khotianv2.data.local.entity.FinancialAccountType.BANK -> com.sabid.khotianv2.domain.model.FinancialAccountType.BANK
}

fun com.sabid.khotianv2.domain.model.FinancialAccountType.toEntity() = when (this) {
    com.sabid.khotianv2.domain.model.FinancialAccountType.CASH -> com.sabid.khotianv2.data.local.entity.FinancialAccountType.CASH
    com.sabid.khotianv2.domain.model.FinancialAccountType.BANK -> com.sabid.khotianv2.data.local.entity.FinancialAccountType.BANK
}

fun CrushingBatchEntity.toDomain() = CrushingBatch(
    id = id,
    batchNumber = batchNumber,
    seedType = seedType,
    seedQuantity = seedQuantity,
    seedRate = seedRate,
    oilQuantity = oilQuantity,
    oilCakeQuantity = oilCakeQuantity,
    wasteQuantity = wasteQuantity,
    crushingCharge = crushingCharge,
    timestamp = timestamp,
    note = note
)

fun CrushingBatch.toEntity() = CrushingBatchEntity(
    id = id,
    batchNumber = batchNumber,
    seedType = seedType,
    seedQuantity = seedQuantity,
    seedRate = seedRate,
    oilQuantity = oilQuantity,
    oilCakeQuantity = oilCakeQuantity,
    wasteQuantity = wasteQuantity,
    crushingCharge = crushingCharge,
    timestamp = timestamp,
    note = note
)

fun AuditLogEntity.toDomain() = AuditLog(
    id = id,
    tableName = tableName,
    recordId = recordId,
    action = action.toDomain(),
    oldValues = oldValuesJson,
    newValues = newValuesJson,
    userId = userId,
    timestamp = timestamp
)

fun AuditLog.toEntity() = AuditLogEntity(
    id = id,
    tableName = tableName,
    recordId = recordId,
    action = action.toEntity(),
    oldValuesJson = oldValues,
    newValuesJson = newValues,
    userId = userId,
    timestamp = timestamp
)

fun com.sabid.khotianv2.data.local.entity.AuditAction.toDomain() = when (this) {
    com.sabid.khotianv2.data.local.entity.AuditAction.INSERT -> com.sabid.khotianv2.domain.model.AuditAction.INSERT
    com.sabid.khotianv2.data.local.entity.AuditAction.UPDATE -> com.sabid.khotianv2.domain.model.AuditAction.UPDATE
    com.sabid.khotianv2.data.local.entity.AuditAction.DELETE -> com.sabid.khotianv2.domain.model.AuditAction.DELETE
}

fun com.sabid.khotianv2.domain.model.AuditAction.toEntity() = when (this) {
    com.sabid.khotianv2.domain.model.AuditAction.INSERT -> com.sabid.khotianv2.data.local.entity.AuditAction.INSERT
    com.sabid.khotianv2.domain.model.AuditAction.UPDATE -> com.sabid.khotianv2.data.local.entity.AuditAction.UPDATE
    com.sabid.khotianv2.domain.model.AuditAction.DELETE -> com.sabid.khotianv2.data.local.entity.AuditAction.DELETE
}
