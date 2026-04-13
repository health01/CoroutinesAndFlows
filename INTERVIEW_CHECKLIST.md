# Kotlin Coroutines & Flow 面試準備 Checklist

> 每個項目都連結到對應的實作檔案，方便複習。
> Each item links to the corresponding implementation file for review.

---

## 🚀 Use Case 1 — Coroutines 基礎

**實作檔案 Implementation files:**
- ViewModel: [`BasicCoroutineViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt)
- Screen: [`BasicCoroutineScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/basic/BasicCoroutineScreen.kt)
- Test: [`BasicCoroutineViewModelTest.kt`](app/src/test/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModelTest.kt)

- [ ] 說明 `launch` vs `async` 的差別與使用時機
  - `launch` → 回傳 `Job`，fire-and-forget，無法取回值
  - `async` → 回傳 `Deferred<T>`，呼叫 `.await()` 取得結果
  - 見 [`BasicCoroutineViewModel.kt` — `demoLaunch()` / `demoAsyncAwait()`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt)

- [ ] 說明 `withContext` 的作用
  - 切換 Dispatcher 並等待 block 完成（同步語義），完成後自動切回
  - 見 [`BasicCoroutineViewModel.kt` — `demoWithContext()`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt)

- [ ] 說明 Structured Concurrency 是什麼、為什麼重要
  - 子 Coroutine 的生命週期受父 Scope 約束
  - 父取消 → 所有子取消；子出現例外 → 向上傳播
  - 確保沒有 leaked coroutine
  - 見 [`BasicCoroutineViewModel.kt` — `demoStructuredConcurrency()`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt)

- [ ] 知道 `viewModelScope` 在哪裡自動取消
  - ViewModel 被清除（`onCleared()`）時自動取消所有子 Coroutine
  - 見 [`BasicCoroutineViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt)

---

## ❌ Use Case 2 — 取消 & 例外處理

**實作檔案 Implementation files:**
- ViewModel: [`CancellationViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/CancellationViewModel.kt)
- Screen: [`CancellationScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/cancellation/CancellationScreen.kt)

- [ ] 說明 `Job.cancel()` 是協作式取消（cooperative cancellation）
  - 只是設定取消旗標，Coroutine 必須到達 suspend point 才真正停止
  - CPU-bound 迴圈需手動檢查 `isActive`
  - 見 [`CancellationViewModel.kt` — `startLongRunningJob()`](app/src/main/java/com/example/coroutinesflows/viewmodel/CancellationViewModel.kt)

- [ ] 說明 `SupervisorJob` vs 普通 `Job` 的差異
  - 普通 Job：任何子失敗 → 所有兄弟及父都被取消
  - SupervisorJob：子失敗互不影響，適合獨立任務
  - 見 [`CancellationViewModel.kt` — `demoSupervisorJob()`](app/src/main/java/com/example/coroutinesflows/viewmodel/CancellationViewModel.kt)

- [ ] 說明 `CoroutineExceptionHandler` 的限制
  - 只對用 `launch` 啟動的 root coroutine 有效
  - `async` 的例外在 `.await()` 時才拋出，需在呼叫端處理
  - 見 [`CancellationViewModel.kt` — `demoExceptionHandler()`](app/src/main/java/com/example/coroutinesflows/viewmodel/CancellationViewModel.kt)

- [ ] 知道為什麼不能吞掉 `CancellationException`
  - 它是 Kotlin 取消協作機制的信號，吞掉會導致 Coroutine 無法正確停止
  - 正確做法：`catch (e: CancellationException) { throw e }`
  - 見 [`CancellationViewModel.kt` — `demoCancellationException()`](app/src/main/java/com/example/coroutinesflows/viewmodel/CancellationViewModel.kt)

---

## 🌊 Use Case 3 — Flow 基礎

**實作檔案 Implementation files:**
- ViewModel: [`BasicFlowViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicFlowViewModel.kt)
- Screen: [`BasicFlowScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/flow/BasicFlowScreen.kt)
- Test: [`FlowOperatorsTest.kt`](app/src/test/java/com/example/coroutinesflows/flow/FlowOperatorsTest.kt)

- [ ] 說明 Cold Flow 的特性
  - 每個 collector 有獨立的執行序列，只有被 `collect` 才開始
  - 見 [`BasicFlowViewModel.kt` — `demoColdFlow()`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicFlowViewModel.kt)

- [ ] 熟悉常用 Intermediate Operators
  - `map`、`filter`、`take` — 見 [`BasicFlowViewModel.kt` — `demoIntermediateOperators()`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicFlowViewModel.kt)
  - `onStart`、`onEach`、`onCompletion` — side-effect operators
  - `catch` — 攔截上游例外，不終止 Flow
  - `transform` — 每個值可 emit 0~N 次，見 [`demoTransform()`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicFlowViewModel.kt)

- [ ] 熟悉常用 Terminal Operators
  - `collect`、`toList`、`first`、`last`、`count`
  - 見 [`BasicFlowViewModel.kt` — `demoTerminalOperators()`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicFlowViewModel.kt)
  - 測試範例: [`FlowOperatorsTest.kt`](app/src/test/java/com/example/coroutinesflows/flow/FlowOperatorsTest.kt)

- [ ] 說明 `catch` 和 try-catch 在 Flow 中的差異
  - `.catch` 只攔截上游例外，可繼續 emit 恢復值
  - 是 operator，可 inline 在 Flow 鏈中
  - 見 [`FlowOperatorsTest.kt` — `catch operator should intercept exception`](app/src/test/java/com/example/coroutinesflows/flow/FlowOperatorsTest.kt)

---

## 📡 Use Case 4 — StateFlow & SharedFlow

**實作檔案 Implementation files:**
- ViewModel: [`StateSharedFlowViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/StateSharedFlowViewModel.kt)
- Screen: [`StateSharedFlowScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/stateflow/StateSharedFlowScreen.kt)

- [ ] 說明 StateFlow vs SharedFlow 各自適用場景
  - `StateFlow` → UI State（有初始值、只保留最新值、新 collector 立即收到當前值）
  - `SharedFlow(replay=0)` → 一次性 Event（導航、Snackbar，不重播）
  - 見 [`StateSharedFlowViewModel.kt` — `_uiState` / `_events`](app/src/main/java/com/example/coroutinesflows/viewmodel/StateSharedFlowViewModel.kt)

- [ ] 說明 private mutable + public read-only 的 backing property 模式
  - `private val _uiState = MutableStateFlow(...)` / `val uiState = _uiState.asStateFlow()`
  - 防止 UI 層直接修改狀態
  - 見 [`StateSharedFlowViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/StateSharedFlowViewModel.kt)

- [ ] 說明 `StateFlow.update {}` 的原子性
  - `_uiState.update { current -> current.copy(...) }` 是 thread-safe 的原子操作
  - 見 [`StateSharedFlowViewModel.kt` — `increment()`](app/src/main/java/com/example/coroutinesflows/viewmodel/StateSharedFlowViewModel.kt)

- [ ] 說明 UI 如何消費一次性 Event
  - `LaunchedEffect(Unit) { events.collectLatest { ... } }`
  - 見 [`StateSharedFlowScreen.kt` — `LaunchedEffect`](app/src/main/java/com/example/coroutinesflows/ui/screens/stateflow/StateSharedFlowScreen.kt)

- [ ] 說明 `collectAsStateWithLifecycle` vs `collectAsState` 的差異
  - `collectAsStateWithLifecycle` → App 進背景時自動停止收集，節省資源（推薦）
  - `collectAsState` → 不感知 Lifecycle，背景也持續收集
  - 見任意 Screen 檔案，例如 [`StateSharedFlowScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/stateflow/StateSharedFlowScreen.kt)

---

## 🗄️ Use Case 5 — Room + Flow 即時更新

**實作檔案 Implementation files:**
- ViewModel: [`RoomFlowViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/RoomFlowViewModel.kt)
- Screen: [`RoomFlowScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/room/RoomFlowScreen.kt)
- DAO: [`NoteDao.kt`](app/src/main/java/com/example/coroutinesflows/data/local/NoteDao.kt)
- Repository: [`NoteRepositoryImpl.kt`](app/src/main/java/com/example/coroutinesflows/data/repository/NoteRepositoryImpl.kt)
- UseCase: [`GetNotesUseCase.kt`](app/src/main/java/com/example/coroutinesflows/domain/usecase/note/GetNotesUseCase.kt)

- [ ] 說明 Room DAO 的 Flow 為何會自動更新
  - Room 透過 `InvalidationTracker` 監聽表格，有 INSERT/UPDATE/DELETE 就重新 emit
  - 見 [`NoteDao.kt` — `observeAllNotes()`](app/src/main/java/com/example/coroutinesflows/data/local/NoteDao.kt)

- [ ] 說明 `stateIn` 的三個參數意義
  - `scope` = viewModelScope
  - `started` = `SharingStarted.WhileSubscribed(5_000)` （最後一個 collector 取消 5 秒後停止）
  - `initialValue` = 初始狀態（必填）
  - 見 [`RoomFlowViewModel.kt` — `notesUiState`](app/src/main/java/com/example/coroutinesflows/viewmodel/RoomFlowViewModel.kt)

- [ ] 說明為什麼推薦 `WhileSubscribed(5000)` 而非 `Eagerly`
  - App 進背景後 5 秒自動停止收集，節省資源；回到前景時重新開始
  - 見 [`RoomFlowViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/RoomFlowViewModel.kt)

- [ ] 說明 Entity ↔ Domain Model mapping 應該在哪一層做
  - 在 Repository 實作層，不在 DAO 或 ViewModel
  - 見 [`NoteRepositoryImpl.kt` — extension functions `toDomain()` / `toEntity()`](app/src/main/java/com/example/coroutinesflows/data/repository/NoteRepositoryImpl.kt)

---

## 🌐 Use Case 6 — Retrofit + Flow 網路請求

**實作檔案 Implementation files:**
- ViewModel: [`NetworkFlowViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModel.kt)
- Screen: [`NetworkFlowScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/network/NetworkFlowScreen.kt)
- UseCase: [`GetPostsUseCase.kt`](app/src/main/java/com/example/coroutinesflows/domain/usecase/post/GetPostsUseCase.kt)
- Repository: [`PostRepositoryImpl.kt`](app/src/main/java/com/example/coroutinesflows/data/repository/PostRepositoryImpl.kt)
- API: [`ApiService.kt`](app/src/main/java/com/example/coroutinesflows/data/remote/ApiService.kt)
- Test: [`NetworkFlowViewModelTest.kt`](app/src/test/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModelTest.kt)

- [ ] 說明用 Flow 管理 Loading/Error/Success 的完整模式
  ```kotlin
  getPostsUseCase()
      .onStart  { emit(Loading) }
      .catch    { emit(Error(it.message)) }
      .collect  { emit(Success(it)) }
  ```
  - 見 [`NetworkFlowViewModel.kt` — `loadPosts()`](app/src/main/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModel.kt)

- [ ] 說明 Retrofit `suspend` 函式不需要手動 `withContext(IO)`
  - Retrofit 內部透過 OkHttp Dispatcher 在後台執行緒完成 I/O
  - 見 [`PostRepositoryImpl.kt`](app/src/main/java/com/example/coroutinesflows/data/repository/PostRepositoryImpl.kt)

- [ ] 說明如何防止 config change 重新發送網路請求
  - `StateFlow` 存在 ViewModel 中，config change 不銷毀 ViewModel
  - UI 重建後訂閱到現有 StateFlow 的當前值，不觸發新請求
  - 見 [`NetworkFlowViewModel.kt` — `_uiState`](app/src/main/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModel.kt)

---

## ⚡ Use Case 7 — 平行 & 順序 API 呼叫

**實作檔案 Implementation files:**
- ViewModel: [`ParallelCallsViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/ParallelCallsViewModel.kt)
- Screen: [`ParallelCallsScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/parallel/ParallelCallsScreen.kt)
- UseCase: [`GetUserWithPostsUseCase.kt`](app/src/main/java/com/example/coroutinesflows/domain/usecase/post/GetUserWithPostsUseCase.kt)

- [ ] 說明如何同時發出多個 API 請求並等待全部完成
  ```kotlin
  coroutineScope {
      val user  = async { getUserById(id) }
      val posts = async { getPostsByUser(id) }
      UserWithPosts(user.await(), posts.await())
  }
  ```
  - 見 [`GetUserWithPostsUseCase.kt`](app/src/main/java/com/example/coroutinesflows/domain/usecase/post/GetUserWithPostsUseCase.kt)

- [ ] 說明平行 vs 循序的時間差異
  - 平行：總時間 ≈ `max(A, B)`
  - 循序：總時間 ≈ `A + B`
  - 見 [`ParallelCallsViewModel.kt` — `loadUserWithPostsParallel()` / `loadUserWithPostsSequential()`](app/src/main/java/com/example/coroutinesflows/viewmodel/ParallelCallsViewModel.kt)

- [ ] 說明 `async` 失敗時在 `coroutineScope` 中的行為
  - 任一 `async` 失敗 → 整個 `coroutineScope` 被取消（structured concurrency）
  - 若要隔離失敗用 `supervisorScope`
  - 見 [`ParallelCallsViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/ParallelCallsViewModel.kt)

- [ ] 說明 `awaitAll()` 的用途
  - 等待 `List<Deferred<T>>` 全部完成，回傳 `List<T>`
  - 任何一個失敗時拋出第一個例外
  - 見 [`ParallelCallsViewModel.kt` — `loadMultipleUsers()`](app/src/main/java/com/example/coroutinesflows/viewmodel/ParallelCallsViewModel.kt)

---

## 🧪 Use Case 8 — Testing Coroutines & Flow

**實作檔案 Implementation files:**
- [`BasicCoroutineViewModelTest.kt`](app/src/test/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModelTest.kt)
- [`NetworkFlowViewModelTest.kt`](app/src/test/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModelTest.kt)
- [`FlowOperatorsTest.kt`](app/src/test/java/com/example/coroutinesflows/flow/FlowOperatorsTest.kt)

- [ ] 說明 `runTest` 的作用
  - 建立 `TestScope`，使用虛擬時鐘，`delay()` 不真正等待
  - 見 [`BasicCoroutineViewModelTest.kt`](app/src/test/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModelTest.kt)

- [ ] 說明 `StandardTestDispatcher` vs `UnconfinedTestDispatcher`
  - `StandardTestDispatcher`：Coroutine 不自動執行，需 `advanceTimeBy` / `advanceUntilIdle`
  - `UnconfinedTestDispatcher`：立即執行，控制性較低
  - 見 [`BasicCoroutineViewModelTest.kt`](app/src/test/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModelTest.kt)

- [ ] 說明 Turbine 的作用與 `.test { }` DSL
  - Flow 測試函式庫，`awaitItem()` / `awaitComplete()` / `awaitError()` 逐步驗證每個 emission
  - 見 [`FlowOperatorsTest.kt` — `turbine - verify each emission`](app/src/test/java/com/example/coroutinesflows/flow/FlowOperatorsTest.kt)

- [ ] 說明 MockK 的基本用法
  - `every { mockUseCase() } returns flowOf(fakeData)` — mock Flow 回傳值
  - `coEvery { ... }` — mock suspend 函式
  - 見 [`NetworkFlowViewModelTest.kt`](app/src/test/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModelTest.kt)

---

## 🔬 Use Case 9 — 進階 Flow 最佳實踐

**實作檔案 Implementation files:**
- ViewModel: [`AdvancedFlowViewModel.kt`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt)
- Screen: [`AdvancedFlowScreen.kt`](app/src/main/java/com/example/coroutinesflows/ui/screens/advanced/AdvancedFlowScreen.kt)

- [ ] 說明搜尋防抖的黃金組合
  ```kotlin
  _query
      .debounce(300)
      .distinctUntilChanged()
      .flatMapLatest { query -> flow { emit(api.search(query)) } }
      .stateIn(viewModelScope, WhileSubscribed(5000), emptyList())
  ```
  - 見 [`AdvancedFlowViewModel.kt` — `searchResults`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt)

- [ ] 說明 backpressure 的三種處理策略
  - `buffer(n)` → 緩衝 N 個值，生產消費並發，見 [`demoBuffer()`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt)
  - `conflate()` → 只保留最新值，跳過中間值，見 [`demoConflate()`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt)
  - `collectLatest` → 新值到來時取消正在進行的 collect block

- [ ] 說明 `shareIn` vs `stateIn` 的差別
  - `shareIn` → `SharedFlow`（無初始值，可設 replay，適合 Event）
  - `stateIn`  → `StateFlow`（必需初始值，只保最新，適合 State）
  - 見 [`AdvancedFlowViewModel.kt` — `expensiveDataSource` / `searchResults`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt)

- [ ] 說明 `shareIn` 在 Repository 層的用途
  - 讓多個 ViewModel 共享同一個上游 Flow，避免重複執行 DB / 網路查詢
  - 見 [`AdvancedFlowViewModel.kt` — `demoShareIn()`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt)

- [ ] 說明 `distinctUntilChanged` 的用途
  - 過濾連續相同的值，避免不必要的 UI 更新
  - 見 [`AdvancedFlowViewModel.kt` — `demoDistinctUntilChanged()`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt)

---

## 🏗️ Clean Architecture 架構

**實作檔案 Implementation files:**
- DI: [`DatabaseModule.kt`](app/src/main/java/com/example/coroutinesflows/di/DatabaseModule.kt) | [`NetworkModule.kt`](app/src/main/java/com/example/coroutinesflows/di/NetworkModule.kt) | [`RepositoryModule.kt`](app/src/main/java/com/example/coroutinesflows/di/RepositoryModule.kt)
- Domain: [`NoteRepository.kt`](app/src/main/java/com/example/coroutinesflows/domain/repository/NoteRepository.kt) | [`GetNotesUseCase.kt`](app/src/main/java/com/example/coroutinesflows/domain/usecase/note/GetNotesUseCase.kt)

- [ ] 說明 `@Binds` vs `@Provides` 的差異
  - `@Binds`：介面 → 實作類別的映射，效率更高（不需要建立 Module 實例）
  - `@Provides`：需要執行程式碼來建立實例時使用
  - 見 [`RepositoryModule.kt`](app/src/main/java/com/example/coroutinesflows/di/RepositoryModule.kt)

- [ ] 說明 UseCase 使用 `operator fun invoke` 的好處
  - 呼叫方式更簡潔：`getNotesUseCase()` 而非 `getNotesUseCase.execute()`
  - 見 [`GetNotesUseCase.kt`](app/src/main/java/com/example/coroutinesflows/domain/usecase/note/GetNotesUseCase.kt)

- [ ] 說明 Domain Model 為何不能依賴任何 Android 類別
  - 保持純 Kotlin，方便在純 JVM 環境測試，見 [`Note.kt`](app/src/main/java/com/example/coroutinesflows/domain/model/Note.kt)
  - 對比 Room Entity [`NoteEntity.kt`](app/src/main/java/com/example/coroutinesflows/data/local/NoteEntity.kt)（有 `@Entity` 等 Android 依賴）

- [ ] 說明資料流方向
  ```
  UI (Compose)
    ↓ collects StateFlow
  ViewModel (StateFlow<UiState>)
    ↓ calls
  UseCase (operator fun invoke)
    ↓ calls
  Repository interface (Domain layer)
    ↓ implemented by
  RepositoryImpl (Data layer)
    ↓ calls
  DAO / ApiService (Data sources)
  ```

---

## 📋 快速查表 Quick Reference

| 概念 | 關鍵 API | 對應檔案 |
|------|---------|---------|
| fire-and-forget | `launch` | [`BasicCoroutineViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt) |
| 取得回傳值 | `async` / `.await()` | [`BasicCoroutineViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt) |
| 切換 Dispatcher | `withContext(IO)` | [`BasicCoroutineViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicCoroutineViewModel.kt) |
| 取消 Coroutine | `job.cancel()` + `isActive` | [`CancellationViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/CancellationViewModel.kt) |
| 隔離子 Coroutine 失敗 | `supervisorScope` | [`CancellationViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/CancellationViewModel.kt) |
| Cold Flow builder | `flow { emit(...) }` | [`BasicFlowViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/BasicFlowViewModel.kt) |
| UI State | `MutableStateFlow` + `.stateIn` | [`RoomFlowViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/RoomFlowViewModel.kt) |
| 一次性 Event | `MutableSharedFlow(replay=0)` | [`StateSharedFlowViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/StateSharedFlowViewModel.kt) |
| Room 即時更新 | `Flow<List<Entity>>` in DAO | [`NoteDao`](app/src/main/java/com/example/coroutinesflows/data/local/NoteDao.kt) |
| 網路狀態管理 | `.onStart` + `.catch` | [`NetworkFlowViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModel.kt) |
| 平行 API 呼叫 | `async` / `awaitAll` | [`GetUserWithPostsUseCase`](app/src/main/java/com/example/coroutinesflows/domain/usecase/post/GetUserWithPostsUseCase.kt) |
| 搜尋防抖 | `debounce` + `flatMapLatest` | [`AdvancedFlowViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt) |
| Backpressure | `buffer` / `conflate` | [`AdvancedFlowViewModel`](app/src/main/java/com/example/coroutinesflows/viewmodel/AdvancedFlowViewModel.kt) |
| 測試 Flow | Turbine `.test { }` | [`FlowOperatorsTest`](app/src/test/java/com/example/coroutinesflows/flow/FlowOperatorsTest.kt) |
| 測試 ViewModel | `runTest` + MockK | [`NetworkFlowViewModelTest`](app/src/test/java/com/example/coroutinesflows/viewmodel/NetworkFlowViewModelTest.kt) |
