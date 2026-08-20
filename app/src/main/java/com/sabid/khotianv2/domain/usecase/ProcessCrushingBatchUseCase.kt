package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.manager.PermissionManager
import com.sabid.khotianv2.domain.manager.SessionManager
import com.sabid.khotianv2.domain.model.*
import com.sabid.khotianv2.domain.repository.CrushingRepository
import java.math.BigDecimal
import javax.inject.Inject

class ProcessCrushingBatchUseCase @Inject constructor(
    private val crushingRepository: CrushingRepository,
    private val permissionManager: PermissionManager,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        batchNumber: String,
        seedType: String,
        seedQuantity: BigDecimal,
        seedRate: BigDecimal,
        oilQuantity: BigDecimal,
        oilCakeQuantity: BigDecimal,
        wasteQuantity: BigDecimal,
        crushingCharge: BigDecimal,
        note: String?
    ): Result<Long> {
        if (!permissionManager.hasPermission(PermissionType.CAN_MANAGE_FACTORY)) {
            return Result.failure(Exception("Permission denied"))
        }

        if (sessionManager.currentUserId.value == null) {
            return Result.failure(Exception("User not authenticated"))
        }

        val batch = CrushingBatch(
            batchNumber = batchNumber,
            seedType = seedType,
            seedQuantity = seedQuantity,
            seedRate = seedRate,
            oilQuantity = oilQuantity,
            oilCakeQuantity = oilCakeQuantity,
            wasteQuantity = wasteQuantity,
            crushingCharge = crushingCharge,
            note = note
        )

        val batchId = crushingRepository.addBatch(batch)

        return Result.success(batchId)
    }
}
