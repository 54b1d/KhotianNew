# Implementation Plan - Ledger Enhancements

This plan outlines the enhancements to the ledger views, including expandable transaction summaries with edit options and enabling party information editing directly from the ledger.

## Proposed Changes

### Navigation & Routes

#### [MODIFY] [NavRoutes.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/navigation/NavRoutes.kt)
- Change `PartyEntry` from `data object` to `data class PartyEntry(val partyId: Long? = null) : NavRoutes`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/MainActivity.kt)
- Update navigation entry for `NavRoutes.PartyEntry` to pass `partyId` to `PartyEntryViewModel`.

---

### Party Ledger

#### [MODIFY] [PartyLedgerScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/PartyLedgerScreen.kt)
- Add state to `PartyLedgerScreen` to track the currently expanded transaction.
- Modify `LedgerRow` to accept an `isExpanded` boolean and toggle it on click.
- Implement an expanded summary view within `LedgerRow` that shows additional details (Note, Quantity, User) and an **Edit** button.
- Add an **Edit** icon (e.g., `Icons.Default.Edit`) to the `TopAppBar` in `PartyLedgerScreen` to navigate to `PartyEntry(partyId = ...)`.

#### [MODIFY] [LedgerViewModel.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/viewmodel/LedgerViewModel.kt)
- No significant changes expected unless needed for expanded state persistence (usually UI state is enough).

---

### Financial Account Ledger

#### [MODIFY] [FinancialAccountLedgerScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/FinancialAccountLedgerScreen.kt)
- Add state to track the expanded transaction.
- Modify `AccountTransactionRow` to show more details and an **Edit** button when expanded.
- Add an **Edit** icon to the `TopAppBar` to navigate to `FinancialAccountEntry(accountId = ...)` if applicable (Wait, user only mentioned Party Ledger for account info, but it makes sense for both).

---

### Party Entry (Editing Support)

#### [MODIFY] [PartyEntryViewModel.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/viewmodel/PartyEntryViewModel.kt)
- Add `partyId: Long?` to the constructor (or via assisted injection if already using it).
- Load existing party data if `partyId` is provided.
- Update `saveParty` to handle both insert and update operations.

#### [MODIFY] [PartyEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/PartyEntryScreen.kt)
- Update title to "Edit Party" when `partyId` is present.
- Ensure all fields are correctly populated from the ViewModel.

## Verification Plan

### Manual Verification
- Navigate to a Party Ledger.
- Tap a transaction row and verify it expands to show details.
- Click "Edit" on the expanded transaction and verify it navigates to `TransactionEntry` with correct data.
- Click the "Edit" icon in the TopAppBar of the Party Ledger and verify it navigates to `PartyEntry` with pre-filled party details.
- Update party details and verify they are reflected back in the ledger.
