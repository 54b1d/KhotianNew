# Khotian New - Project Initialization

This project is initialized with **MVVM + Clean Architecture**, **Dagger Hilt** for dependency injection, and **Supabase Kotlin SDK**.

## Project Structure

```mermaid
graph TD
    subgraph app
        subgraph src/main/java/com/sabid/khotianv2
            subgraph data
                DR[repository]
                DL[local]
                DRE[remote]
            end
            subgraph domain
                DM[model]
                DO[repository]
                DU[usecase]
            end
            subgraph presentation
                PV[viewmodel]
                PU[ui]
            end
            subgraph di
                HiltModules
            end
            App[KhotianApp.kt]
            MA[MainActivity.kt]
        end
    end

    MA --> PV
    PV --> DU
    DU --> DO
    DR -- implements --> DO
    DR --> DRE
    DR --> DL
    HiltModules -- provides --> DR
    HiltModules -- provides --> SupabaseClient
```

## Layers Description

- **Data Layer**: Responsible for data sources (Remote: Supabase, Local: Room). Implementation of repositories.
- **Domain Layer**: Contains Business Logic. Defines Repository Interfaces, Models, and UseCases.
- **Presentation Layer**: UI (Jetpack Compose) and ViewModels.
- **DI (Dagger Hilt)**: Dependency Injection setup for the entire app.

## Technologies Used

- **Jetpack Compose**: Modern UI toolkit.
- **Dagger Hilt**: Dependency Injection.
- **Supabase Kotlin SDK**: Backend as a Service (Auth, Database, Realtime).
- **Kotlinx Serialization**: JSON serialization.
- **Ktor**: HTTP client for Supabase.
- **Room**: Local persistence.

## Setup Instructions

1.  **Supabase Configuration**: Update `SupabaseModule.kt` with your `supabaseUrl` and `supabaseKey`.
2.  **API Keys**: Do not commit real keys to version control. Use `local.properties` or environment variables for production.

render_diffs(file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/app/build.gradle.kts)
render_diffs(file:///C:/Users/Administrator/AndroidStudioProjects/KhotianNew/gradle/libs.versions.toml)
