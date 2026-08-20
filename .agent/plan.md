# Project Plan

A lightweight, secure, local-first Accounting, Inventory & Factory Crushing App for Android.
Key features:
- Supabase Master Auth & Local Multi-User RBAC (Admin, Operator, Viewer roles).
- High-Density/Compact UI Design (Jetpack Compose, Material 3).
- Offline-first with Room DB & Cloud Sync (Supabase Storage/Postgrest backups).
- In-App Updates via Supabase.
- Core Business Logic: Transportation Accounting, Factory Crushing (Seed to Oil/Cake), Unified Party Ledger, Audit Logs.
- Data Precision: Long/BigDecimal, Integer rounding for display.
- Architecture: MVVM + Clean Architecture + Dagger Hilt.

## Project Brief

# Khotian New: Project Brief

A lightweight, secure, local-first Accounting, Inventory, and Factory Crushing application designed for high-density data management on Android.

## Features (MVP)
*   **Factory Crushing & Inventory Tracking**: Record the conversion process of seeds into oil and cake, with real-time inventory management for both raw materials and finished products.
*   **Unified Party Ledger & Transportation Accounting**: Centralized ledger for tracking transactions with suppliers, customers, and transport services, including precision-calculated freight and crushing charges using `BigDecimal`.
*   **Local-First Multi-User RBAC**: Secure local environment with Role-Based Access Control (Admin, Operator, Viewer) to ensure data integrity and restricted access based on user roles.
*   **Offline-First Data Sync**: Robust data management using Room for seamless offline operations, with automated background synchronization to Supabase for cloud backups and cross-device consistency.

## High-Level Technical Stack
*   **Kotlin**: Primary programming language for modern, safe, and expressive code.
*   **Jetpack Compose**: Declarative UI toolkit for building a high-density, compact, and reactive Material 3 interface.
*   **Jetpack Navigation 3**: A state-driven navigation approach to handle complex app flows predictably.
*   **Compose Material Adaptive**: Essential for creating layouts that adapt seamlessly to phones, tablets, and foldables.
*   **Coroutines & Flow**: For efficient asynchronous task management and reactive data streams.
*   **Dagger Hilt**: Standard dependency injection library to support a modular Clean Architecture.
*   **Room DB**: Local persistence layer required for the offline-first experience.
*   **Supabase**: Integrated for Master Authentication, Postgrest-based data synchronization, and In-App Update delivery.
*   **MVVM + Clean Architecture**: Structural pattern to decouple business logic from UI and data sources.

## Implementation Steps
**Total Duration:** 3h 16m 44s

### Task_1_ProjectScaffolding: Initialize the project with MVVM + Clean Architecture folder structure, setup Dagger Hilt for dependency injection, and configure Supabase with API keys.
- **Status:** COMPLETED
- **Updates:** Initialized the Khotian New project with MVVM + Clean Architecture folder structure.
- **Acceptance Criteria:**
  - Project folder structure follows MVVM + Clean Architecture
  - Dagger Hilt is successfully integrated
  - Supabase URL and API Key are configured in the project
  - Project builds successfully
- **Duration:** 46m 56s

### Task_2_DataPersistenceRBAC: Define foundational Room entities (User, Role, Permission, Transaction, CrushingBatch, AuditLog) and implement the PermissionManager for local Role-Based Access Control (RBAC).
- **Status:** COMPLETED
- **Updates:** Defined 6 foundational Room entities (UserEntity, RoleEntity, PermissionEntity, TransactionEntity, CrushingBatchEntity, AuditLogEntity).
- **Acceptance Criteria:**
  - All 6 foundational Room entities are correctly defined
  - Room database initializes and builds without errors
  - PermissionManager correctly validates User-Role-Permission mappings
  - Build pass
- **Duration:** 7m 51s

### Task_3_BusinessLogicRepository: Implement the domain and data layers, including Repositories and Use Cases for Accounting (Transactions) and Factory (Crushing) operations using Coroutines and Flow.
- **Status:** COMPLETED
- **Updates:** Implemented domain models, repository interfaces, and use cases for Accounting and Factory Crushing.
- **Acceptance Criteria:**
  - Repositories handle Transaction and CrushingBatch CRUD operations
  - Financial precision handled using Long/BigDecimal as per requirements
  - Flow used for reactive data streams from Room to UI
  - Build pass
- **Duration:** 5m 34s

### Task_4_HighDensityUI: Develop High-Density Jetpack Compose UI for Transaction Entry and Unified Ledger using Material 3, Navigation 3, and Adaptive Layouts.
- **Status:** COMPLETED
- **Updates:** Completed the missing high-density UI components (Crushing Entry) and enhanced the Ledger with running balances.
- **Acceptance Criteria:**
  - Transaction Entry screen implemented with high-density layout
  - Unified Ledger screen displays reactive transaction data
  - Navigation 3 state-driven flow is functional
  - Adaptive layouts are responsive to different window sizes
  - The implemented UI must match the design provided in [Insert UI design image Path].
- **Duration:** 20m 47s

### Task_5_SyncAuditUpdate: Integrate Supabase Auth, background Sync logic, Audit Log history engine, and the In-App Update mechanism.
- **Status:** COMPLETED
- **Updates:** Fixed the Moshi BigDecimal adapter crash by creating and registering a BigDecimalAdapter. Fixed the WorkManager instantiation crash by implementing Configuration.Provider in the Application class and properly configuring HiltWorkerFactory. Verified the fix by checking file contents and successful build.
- **Acceptance Criteria:**
  - Supabase Auth login/logout is functional
  - Offline-first sync with Supabase works in the background
  - AuditLog captures all transaction modifications
  - App successfully checks Supabase for version updates
  - App does not crash
- **Duration:** 41m 25s

### Task_6_RunAndVerify: Perform final verification of the application to ensure stability, performance, and alignment with all project requirements.
- **Status:** COMPLETED
- **Updates:** Final QA check completed by critic_agent. The app is stable (no crashes), the High-Density UI principles are correctly applied, RBAC is functional, and adaptive layouts work on large screens. All core business logic (Unified Ledger with running balances, Factory Crushing) is verified. Minor UI tweaks noted for future refinement.
- **Acceptance Criteria:**
  - Application stability verified (no crashes)
  - Alignment with user requirements confirmed
  - Critical UI issues reported and resolved
  - All existing tests pass
  - Build pass
- **Duration:** 1h 14m 11s

