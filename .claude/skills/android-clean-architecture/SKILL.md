---
name: android-clean-architecture
description: >
  Guide for implementing Clean Architecture in this Android project. Use when user asks about
  architecture patterns, where to put files, how to structure code, dependency injection with Hilt,
  or wants to follow project conventions for data/domain/ui layers.
---

# Android Clean Architecture

## When to Use

- User asks "where should I put this file?"
- User wants to understand the architecture
- User asks about Hilt DI patterns
- User needs to create new UseCase, Repository, ViewModel, or Screen
- User asks "how does data flow in this project?"

## Architecture Overview

```
UI Layer (Compose)
    ↓
ViewModel Layer (StateFlow<SealedUiState>)
    ↓
UseCase Layer (domain/usecase - single operator fun invoke)
    ↓
Repository Layer (data/repository - implements domain interfaces)
    ↓
Data Sources (data/api, data/db)
```

## Package Structure

```
com.jsation.astroforge/
├── data/
│   ├── api/           # Retrofit services, NetworkModule
│   ├── model/         # Data classes for API
│   ├── db/            # Room database, DAOs, entities
│   ├── repository/    # Repository implementations
│   └── module/        # Hilt modules (DatabaseModule, RepositoryModule)
├── domain/
│   ├── repository/    # Kotlin interfaces
│   └── usecase/       # Single-use-case classes
├── viewmodel/         # @HiltViewModel classes
└── ui/
    ├── screens/       # Compose screens
    └── theme/         # Material3 theme
```

## Patterns

### UseCase Pattern

```kotlin
class GetBirthChartUseCase @Inject constructor(
    private val repository: ChartRepository
) {
    suspend operator fun invoke(params: GetBirthChartParams): Result<BirthChart> =
        repository.getBirthChart(params)
}
```

### Repository Pattern

```kotlin
class ChartRepositoryImpl @Inject constructor(
    private val api: AstroApi,
    private val dao: ChartDao
) : ChartRepository {
    override suspend fun getBirthChart(params: BirthChartParams): Result<BirthChart> =
        runCatching {
            val response = api.getBirthChart(params)
            // process and return
        }
}
```

### ViewModel Pattern

```kotlin
@HiltViewModel
class ChartViewModel @Inject constructor(
    private val getBirthChartUseCase: GetBirthChartUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Idle)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    fun loadChart(params: BirthChartParams) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading
            getBirthChartUseCase(params)
                .onSuccess { _uiState.value = ChartUiState.Success(it) }
                .onFailure { _uiState.value = ChartUiState.Error(it.message) }
        }
    }
}

sealed class ChartUiState {
    object Idle : ChartUiState()
    object Loading : ChartUiState()
    data class Success(val data: BirthChart) : ChartUiState()
    data class Error(val message: String) : ChartUiState()
}
```

### Compose Screen Pattern

```kotlin
@Composable
fun BirthInputScreen(
    viewModel: ChartViewModel = hiltViewModel(),
    onNavigateToChart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is ChartUiState.Idle -> /* input form */
        is ChartUiState.Loading -> /* loading indicator */
        is ChartUiState.Success -> /* navigate or show data */
        is ChartUiState.Error -> /* error message */
    }
}
```

### Hilt Module Pattern

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideChartRepository(
        api: AstroApi,
        dao: ChartDao
    ): ChartRepository = ChartRepositoryImpl(api, dao)
}
```

## Key Dependencies

| Layer | Provides |
|-------|----------|
| `data/api/NetworkModule` | Retrofit, OkHttp, API interface |
| `data/module/DatabaseModule` | Room database, DAOs |
| `data/module/RepositoryModule` | Repository implementations |
| `viewmodel/` | HiltViewModels auto-provided via @HiltViewModel |

## Important Notes

- **NetworkModule lives in `data/api/`** — intentional inconsistency with other modules in `data/module/`
- **`fallbackToDestructiveMigration`** gated to DEBUG mode only
- **SQLCipher** passphrase currently hardcoded — Phase 4 TODO to migrate to Android Keystore
- **Unit tests**: Use MockK, NOT Robolectric. Mock DAOs, don't use `Room.inMemoryDatabaseBuilder`

## Where to Put New Code

| Type | Location |
|------|----------|
| API endpoint | `data/api/AstroApi.kt` |
| API request/response | `data/model/` |
| Room entity | `data/db/ChartEntity.kt` |
| DAO | `data/db/ChartDao.kt` |
| Repository | `data/repository/ChartRepositoryImpl.kt` |
| Domain interface | `domain/repository/ChartRepository.kt` |
| UseCase | `domain/usecase/GetBirthChartUseCase.kt` |
| ViewModel | `viewmodel/ChartViewModel.kt` |
| Screen | `ui/screens/BirthInputScreen.kt` |
| Hilt module | `data/module/` or colocate with feature |
