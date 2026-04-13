package com.example.coroutinesflows.flow

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Flow Operators 單元測試。
 * Unit tests for Flow operators.
 *
 * 測試原則 Testing principles:
 * 1. runTest 讓 delay() 在虛擬時鐘中執行，不會真的等待。
 *    runTest makes delay() use virtual time — no real waiting.
 * 2. Turbine 的 .test { } 讓你逐個驗證每個 emission。
 *    Turbine's .test { } lets you assert each emission step by step.
 * 3. 測試 Flow 的 error path 用 .catch 確認 exception 被攔截。
 *    Test Flow error paths by asserting .catch behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FlowOperatorsTest {

    // ── Test: map operator ────────────────────────────────────────────────
    @Test
    fun `map operator should transform each emission`() = runTest {
        val result = flowOf(1, 2, 3)
            .map { it * 2 }
            .toList()

        assertEquals(listOf(2, 4, 6), result)
    }

    // ── Test: filter operator ─────────────────────────────────────────────
    @Test
    fun `filter operator should keep only matching items`() = runTest {
        val result = flowOf(1, 2, 3, 4, 5)
            .filter { it % 2 == 0 }
            .toList()

        assertEquals(listOf(2, 4), result)
    }

    // ── Test: take operator ───────────────────────────────────────────────
    @Test
    fun `take operator should limit emissions`() = runTest {
        val result = flow { for (i in 1..100) emit(i) }
            .take(3)
            .toList()

        assertEquals(listOf(1, 2, 3), result)
        assertEquals(3, result.size)
    }

    // ── Test: catch operator ──────────────────────────────────────────────
    @Test
    fun `catch operator should intercept exception and emit fallback`() = runTest {
        val result = flow<Int> {
            emit(1)
            throw RuntimeException("Oops!")
        }
            .catch { emit(-1) }  // Fallback value on error
            .toList()

        assertEquals(listOf(1, -1), result)
    }

    // ── Test: onStart emission ────────────────────────────────────────────
    @Test
    fun `onStart should emit loading state before actual data`() = runTest {
        val states = mutableListOf<String>()

        flow { emit("data") }
            .onStart { emit("loading") }
            .collect { states.add(it) }

        assertEquals(listOf("loading", "data"), states)
    }

    // ── Test: Turbine — test complex flow emissions ───────────────────────
    /**
     * 用 Turbine 驗證複雜 Flow 的每個 emission 順序。
     * Use Turbine to verify each emission of a complex flow in order.
     */
    @Test
    fun `turbine - verify each emission in order`() = runTest {
        flow {
            emit("first")
            delay(100)
            emit("second")
            delay(100)
            emit("third")
        }.test {
            assertEquals("first",  awaitItem())
            assertEquals("second", awaitItem())
            assertEquals("third",  awaitItem())
            awaitComplete() // Verify the flow completed normally
        }
    }

    // ── Test: Turbine — error handling ────────────────────────────────────
    @Test
    fun `turbine - should capture errors`() = runTest {
        flow<String> {
            emit("before error")
            throw IllegalStateException("test error")
        }.test {
            assertEquals("before error", awaitItem())
            val error = awaitError()
            assertTrue(error is IllegalStateException)
            assertEquals("test error", error.message)
        }
    }

    // ── Test: chain of operators ──────────────────────────────────────────
    @Test
    fun `chained operators should compose correctly`() = runTest {
        val result = flowOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .filter { it % 2 == 0 }     // 2, 4, 6, 8, 10
            .map { it * it }             // 4, 16, 36, 64, 100
            .take(3)                     // 4, 16, 36
            .toList()

        assertEquals(listOf(4, 16, 36), result)
    }
}
