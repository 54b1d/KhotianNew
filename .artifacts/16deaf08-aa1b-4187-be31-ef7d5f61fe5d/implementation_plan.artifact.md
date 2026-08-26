# Filterable Selectors and Clear Icons

Add keyboard filtering capability to account and other selector fields in `TransactionEntryScreen`. Also add a clear icon to all `OutlinedTextField` components throughout the application to improve user experience.

## User Review Required

> [!NOTE]
> The account selectors will now allow typing. The dropdown will filter results based on the typed text. If the user clears the text or clicks the clear icon, the selection will be reset.

> [!IMPORTANT]
> The "Clear" icon will be added to ALL edit fields. This includes numeric fields, text notes, and dropdown selectors.

## Proposed Changes

### Presentation Layer (UI)

#### [MODIFY] [TransactionEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/TransactionEntryScreen.kt)
- Update all `ExposedDropdownMenuBox` selectors (Source Account, Destination Account, Party, Product, Unit, Financial Account, Expense Category) to:
    - Allow text input (`readOnly = false`).
    - Use a local `searchQuery` state to filter the list of items.
    - Update the search query when an item is selected.
    - Show a clear icon when text is present to reset the selection.
- Add clear icons to all other `OutlinedTextField`s (Amount, Quantity, Rate, Note, etc.).

#### [MODIFY] [FinancialAccountEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/FinancialAccountEntryScreen.kt)
- Add clear icons to name and opening balance fields.

#### [MODIFY] [PartyEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/PartyEntryScreen.kt)
- Add clear icons to name, phone, address, and opening balance fields.

#### [MODIFY] [ProductEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/ProductEntryScreen.kt)
- Add clear icons to name and price fields.

#### [MODIFY] [SetupScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/SetupScreen.kt)
- Add clear icons to business name and address fields.

#### [MODIFY] [StocktakeScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/StocktakeScreen.kt)
- Add clear icons to quantity and note fields.

#### [MODIFY] [UnitEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/UnitEntryScreen.kt)
- Add clear icons to name and factor fields.

#### [MODIFY] [CrushingEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/CrushingEntryScreen.kt)
- Add clear icons to all input fields.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/LoginScreen.kt)
- Add clear icons to username and password fields.

#### [MODIFY] [PartiesScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/PartiesScreen.kt)
- Add clear icon to the search field.

## Verification Plan

### Manual Verification
1.  Navigate to **Transaction Entry**.
2.  Click on "From Account" and start typing. Verify the list is filtered.
3.  Select an account. Verify the field shows the selected account name.
4.  Click the 'X' (clear) icon in the field. Verify the selection is cleared.
5.  Check other fields (Amount, Note) and verify the 'X' icon appears when typing and clears the text when clicked.
6.  Verify similar behavior in other screens (Party Entry, Product Entry, etc.).
