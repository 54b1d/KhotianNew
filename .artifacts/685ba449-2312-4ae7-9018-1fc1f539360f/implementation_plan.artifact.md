# Implementation Plan - Contra/Transfer Transaction System

Implement a "Contra/Transfer" transaction system to move money between Financial Accounts (Bank to Cash, Cash to Bank, Bank to Bank).

## User Review Required

> [!IMPORTANT]
> This change introduces a new transaction type `TRANSFER` which requires two financial accounts (Source and Destination). Existing backup files will need to be updated to include the new `toFinancialAccountId` field if they are to be restored after this change, although the system will remain backward compatible for old backups (the field will be null).

## Proposed Changes

### Data Layer

#### [MODIFY] [BusinessEntities.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/local/entity/BusinessEntities.kt)
- `TransactionEntity` already has `toFinancialAccountId: Long? = null` and `TransactionType` has `TRANSFER`. `BusinessTransactionType` also has `TRANSFER`.
- I will verify if I need to add any constraints or indices.

#### [MODIFY] [BusinessDaos.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/local/dao/BusinessDaos.kt)
- `FinancialAccountDao`: Add a `@Transaction` method to update both accounts' balances during a transfer.
- `TransactionDao`: Update `getTransactionsByAccount` to fetch where `financialAccountId = :accountId OR toFinancialAccountId = :accountId`.

#### [MODIFY] [Mappers.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/repository/Mappers.kt)
- Update `toDomain()` and `toEntity()` for `Transaction` to include `toFinancialAccountId`.
- Update enum mappers for `TransactionType` and `BusinessTransactionType` to handle `TRANSFER`.

### Domain Layer

#### [MODIFY] [AddTransactionUseCase.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/domain/usecase/AddTransactionUseCase.kt)
- Update `invoke` parameters: make `partyId` nullable.
- Add `toFinancialAccountId: Long? = null` parameter.
- Add logic for `BusinessTransactionType.TRANSFER`:
    - Validate that both `financialAccountId` and `toFinancialAccountId` are provided and are different.
    - Set `TransactionType.TRANSFER`.
    - Update balances for both accounts (Decrease source, Increase destination).

#### [MODIFY] [GetAccountLedgerUseCase.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/domain/usecase/GetAccountLedgerUseCase.kt)
- The repository already returns the updated list, but I might need to ensure the labels are correct in the UI or domain model.

### UI Components

#### [MODIFY] [TransactionEntryViewModel.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/viewmodel/TransactionEntryViewModel.kt)
- Add `toFinancialAccountId` state.
- Update validation and submission logic to handle transfers.

#### [MODIFY] [TransactionEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/TransactionEntryScreen.kt)
- Update UI to show "From Account" and "To Account" dropdowns when `TRANSFER` is selected.
- Hide Party and Product fields for transfers.

#### [MODIFY] [FinancialAccountLedgerScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/FinancialAccountLedgerScreen.kt)
- Update `AccountTransactionRow` to display "Transfer In" or "Transfer Out" with the other account's name.

## Verification Plan

### Automated Tests
- Create a unit test for `AddTransactionUseCase` to verify transfer logic (balance updates for both accounts).
- Create a unit test for `FinancialAccountDao` (instrumented) to verify balance updates in a transaction.

### Manual Verification
- Record a "Cash to Bank" transfer and verify:
    - Cash account balance decreases.
    - Bank account balance increases.
    - Both ledgers show the transfer transaction correctly.
- Record a "Bank to Cash" transfer and verify balances.
- Verify backup and restore still works and includes transfer data.
