# Implement Bottom Navigation

The goal is to improve application navigation efficiency by introducing a `NavigationBar` in the `MainActivity`. This will allow users to switch between the main sections (Home, Parties, and Dashboard) quickly from anywhere in the top-level app context.

## Proposed Changes

### Navigation Structure

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/MainActivity.kt)
- Wrap the `NavDisplay` in a `Scaffold`.
- Implement a `NavigationBar` that contains items for:
    - **Home** (`Icons.Rounded.Home`)
    - **Parties** (`Icons.Rounded.People`)
    - **Dashboard** (`Icons.Rounded.Dashboard`)
- The `NavigationBar` will be visible when the current route is `Home`, `Parties`, or `Dashboard`.
- Tapping a `NavigationBarItem` will navigate to the respective route, clearing the backstack to ensure a clean navigation state.

### UI Refinement

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/HomeScreen.kt)
- Remove the "Parties" and "Dashboard" icons from the `CenterAlignedTopAppBar` actions, as these are now accessible via the bottom bar.

#### [MODIFY] [PartiesScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/PartiesScreen.kt)
- Remove the `navigationIcon` (Back button) from the `TopAppBar` since it will be a top-level destination.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/src/main/java/com/sabid/khotianv2/presentation/ui/DashboardScreen.kt)
- Remove the `navigationIcon` (Back button) from the `TopAppBar`.
- **Redesign Navigation Actions**: Replace the current vertical stack of multiple Floating Action Buttons (FABs) with a cleaner UI:
    - Keep only the primary "Add Transaction" as a standard FAB.
    - Move other management actions (Crushing, Accounts, Backup, P&L, Inventory, Units, Add Party) into a new "Quick Actions" or "Management Tools" grid section within the `LazyVerticalGrid`.
    - Use consistent iconography and labels for these grid items.

## Verification Plan

### Manual Verification
1.  **Launch the App**: Ensure the app starts on the `Home` screen with the bottom navigation bar visible.
2.  **Switch Tabs**: Tap on "Parties" and "Dashboard" items in the bottom bar. Verify that the correct screen is displayed and the icon is highlighted.
3.  **Navigate to Detail**: Tap on a transaction in `Home` or a party in `Parties`. Verify that the detail screen (e.g., `TransactionEntry` or `PartyLedger`) opens and the bottom navigation bar disappears (or stays if intended, but standard behavior is to hide it on deep details).
4.  **Back Navigation**: Navigate back from a detail screen and verify the bottom navigation bar reappears on the top-level screen.
