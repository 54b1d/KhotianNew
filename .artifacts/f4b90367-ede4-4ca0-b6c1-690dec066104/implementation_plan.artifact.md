# Implementation Plan - Fix TransactionEntryScreen UI

Improve the `TransactionEntryScreen` UI by refining the Transaction Type selection and enhancing the clarity of the `TRANSFER` (Contra Entry) layout.

## User Review Required

> [!IMPORTANT]
> I will replace the current `Row` of `FilterChip`s with a `SingleChoiceSegmentedButtonRow`. Due to the length of some labels (e.g., "PAYMENT RECEIVED"), I will use shortened or formatted labels to ensure they fit properly on most screens. If it remains too cramped, I will fall back to a `FlowRow` with `FilterChip`s.

## Proposed Changes

### Presentation Layer

#### [MODIFY] [TransactionEntryScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/TransactionEntryScreen.kt)

- **Transaction Type Selection**:
    - Replace the current `Row` of `FilterChip`s with `SingleChoiceSegmentedButtonRow`.
    - Use `SegmentedButton` for each `BusinessTransactionType`.
    - Ensure labels are clear but compact (e.g., "Pay In" for `PAYMENT_RECEIVED`, "Pay Out" for `PAYMENT_MADE`). Actually, I'll stick to the user's requested names but maybe use abbreviations if needed, or just let M3 handle the layout if possible. Wait, the user listed: `Purchase, Sale, Payment, Transfer`.
    - Wait, `BusinessTransactionType` has 5 entries. `PAYMENT_MADE` and `PAYMENT_RECEIVED` are both "Payment". I should probably show them as separate options as they are in the enum.
    - I'll use a `SingleChoiceSegmentedButtonRow` wrapped in a `HorizontalPager` or just a `Row` with `horizontalScroll` if it overflows, but usually `SegmentedButtonRow` is meant to fit.
    - Alternatively, I'll use `FlowRow` with `FilterChip` as it's more flexible for 5 items. The prompt mentions: "If using Chips, ensure they are in a FlowRow or a ScrollableRow... Alternatively, use a SingleChoiceSegmentedButtonRow". I'll try `SingleChoiceSegmentedButtonRow` with shortened labels first.

- **Transfer Layout**:
    - When `TRANSFER` is selected, group "From Account" and "To Account" more clearly.
    - Use a `Row` for these two fields if they fit, or keep them stacked but with a clear visual separator or grouping.
    - Ensure `imePadding()` and `verticalScroll` are working correctly to prevent the keyboard from blocking input.

- **Styling**:
    - Ensure all fields use `MaterialTheme.typography.bodySmall` for a high-density look as requested.
    - Use consistent spacing (`Arrangement.spacedBy(8.dp)`).

## Verification Plan

### Automated Tests
- Run the build to ensure no compilation errors: `./gradlew :app:assembleDebug`

### Manual Verification
- Open `TransactionEntryScreen` in the emulator.
- Verify that all 5 transaction types are visible and selectable.
- Select `TRANSFER` and verify that both "From Account" and "To Account" are clearly visible.
- Check that the keyboard doesn't obscure the active field.
