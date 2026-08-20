# Implementation Plan - Data Import/Export & Opening Balance

Implement a robust Data Import/Export system and add support for opening balances for parties and products.

## User Review Required

> [!IMPORTANT]
> The import process will handle conflicts for Parties by their **name**. If a party with the same name already exists, the import will skip that party or merge data (to be decided, default will be skip/ignore to avoid duplication).

## Proposed Changes

### Data Layer

#### [MODIFY] [BusinessEntities.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/local/entity/BusinessEntities.kt)
- Add `openingBalance: BigDecimal = BigDecimal.ZERO` to `PartyEntity`.
- Add `openingBalance: BigDecimal = BigDecimal.ZERO` to `ProductEntity`.

#### [MODIFY] [BusinessDaos.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/local/dao/BusinessDaos.kt)
- Add `insertParties(parties: List<PartyEntity>)` etc. for batch operations.
- Add `getPartyByName(name: String): PartyEntity?` to check for duplicates.

#### [MODIFY] [Mappers.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/repository/Mappers.kt)
- Update `toDomain` and `toEntity` for `Party` and `Product`.

#### [NEW] [DataSyncRepositoryImpl.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/repository/DataSyncRepositoryImpl.kt)
- Implement JSON export/import logic using Moshi.
- Handle Room transactions for safety.

---

### Domain Layer

#### [MODIFY] [BusinessModels.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/domain/model/BusinessModels.kt)
- Add `openingBalance: BigDecimal` to `Party` and `Product`.

#### [NEW] [DataSyncRepository.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/domain/repository/DataSyncRepository.kt)
- Define `exportData(): String` (JSON) and `importData(json: String): Result<Unit>`.

#### [MODIFY] [GetUnifiedLedgerUseCase.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/domain/usecase/GetUnifiedLedgerUseCase.kt)
- Include `openingBalance` in the running balance calculation.

---

### Presentation Layer

#### [NEW] [BackupScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/BackupScreen.kt)
- UI for Export and Import actions.

#### [NEW] [BackupViewModel.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/viewmodel/BackupViewModel.kt)
- Manage backup/restore states and logic.

#### [MODIFY] [PartyEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/PartyEntryScreen.kt)
- Add TextField for `openingBalance`.

#### [MODIFY] [ProductEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/ProductEntryScreen.kt)
- Add TextField for `openingBalance`.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/DashboardScreen.kt)
- Add `ModalNavigationDrawer` with "Backup & Restore" item.

#### [MODIFY] [NavRoutes.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/navigation/NavRoutes.kt)
- Add `Backup` route.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/MainActivity.kt)
- Register `Backup` route in `NavDisplay`.

## Verification Plan

### Automated Tests
- Unit tests for `DataSyncRepositoryImpl` to verify JSON parsing and database interaction.
- Unit tests for `GetUnifiedLedgerUseCase` to verify balance calculation with `openingBalance`.

### Manual Verification
1. Open Party Entry, set an opening balance, and verify it appears in the Ledger.
2. Export data, check the JSON file.
3. Import the JSON file into a fresh install (or after clearing data) and verify all records are restored.
4. Verify navigation drawer works as expected.
