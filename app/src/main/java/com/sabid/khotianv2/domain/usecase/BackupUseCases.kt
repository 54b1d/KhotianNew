package com.sabid.khotianv2.domain.usecase

import com.sabid.khotianv2.domain.repository.BackupRepository
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): String = backupRepository.exportData()
}

class ImportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(jsonData: String) = backupRepository.importData(jsonData)
}
