# Add More Preloaded Data for Testing

This plan expands the `SampleDataGenerator` to include a wider variety of financial accounts and transaction types, ensuring comprehensive coverage for testing and bypass mode.

## Proposed Changes

### Data Layer

#### [MODIFY] [SampleDataGenerator.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/data/local/SampleDataGenerator.kt)

Expand the `generateSampleData` method to include:
- **New Account**: Add a mobile banking account (e.g., bKash).
- **New Parties**: Add an investor party and more diverse customers.
- **Transaction Types**:
    - `EQUITY_CONTRIBUTION`: Initial investment.
    - `PAYMENT_RECEIVED`: Customer payment.
    - `EXPENSE`: Direct expense (Rent, Labor).
    - `STOCK_ADJUSTMENT`: Inventory correction.
    - `PROFIT_DISTRIBUTION`: Withdrawal of profits.
    - `TRANSFER`: Between different account types (Bank to Mobile).

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Trigger "Bypass & Preload Data" from the `LoginScreen` or `SetupScreen`.
- Verify that the new accounts (Main Cash, Business Bank, bKash) appear with correct balances.
- Verify that the transaction history shows all types (Purchase, Sale, Payment, Expense, Equity, etc.).
- Check party ledgers to ensure transactions are correctly associated.
