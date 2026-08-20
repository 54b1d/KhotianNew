package com.sabid.khotianv2.data.local.converter

import androidx.room.TypeConverter
import com.sabid.khotianv2.data.local.entity.AuditAction
import com.sabid.khotianv2.data.local.entity.TransactionType
import com.sabid.khotianv2.domain.model.PermissionType
import java.math.BigDecimal

class AppConverters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }

    @TypeConverter
    fun fromPermissionType(value: PermissionType): String = value.name

    @TypeConverter
    fun toPermissionType(value: String): PermissionType = PermissionType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromBusinessTransactionType(value: com.sabid.khotianv2.data.local.entity.BusinessTransactionType): String = value.name

    @TypeConverter
    fun toBusinessTransactionType(value: String): com.sabid.khotianv2.data.local.entity.BusinessTransactionType = com.sabid.khotianv2.data.local.entity.BusinessTransactionType.valueOf(value)

    @TypeConverter
    fun fromAuditAction(value: AuditAction): String = value.name

    @TypeConverter
    fun toAuditAction(value: String): AuditAction = AuditAction.valueOf(value)

    @TypeConverter
    fun fromFreightType(value: com.sabid.khotianv2.data.local.entity.FreightType): String = value.name

    @TypeConverter
    fun toFreightType(value: String): com.sabid.khotianv2.data.local.entity.FreightType = com.sabid.khotianv2.data.local.entity.FreightType.valueOf(value)
}
