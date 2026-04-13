package com.example.coroutinesflows.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coroutinesflows.ui.navigation.Screen

private data class DemoItem(
    val screen: Screen,
    val description: String
)

private val demoItems = listOf(
    DemoItem(Screen.BasicCoroutine,  "launch / async-await / withContext / structured concurrency"),
    DemoItem(Screen.Cancellation,    "Job 取消 / SupervisorJob / CoroutineExceptionHandler"),
    DemoItem(Screen.BasicFlow,       "Cold Flow / 中間操作符 / 終端操作符"),
    DemoItem(Screen.StateSharedFlow, "UI State vs UI Event 區分"),
    DemoItem(Screen.RoomFlow,        "Room DAO Flow + stateIn + CRUD"),
    DemoItem(Screen.NetworkFlow,     "Retrofit + Flow + Loading/Error/Empty 狀態"),
    DemoItem(Screen.ParallelCalls,   "async 平行呼叫 vs 循序呼叫效能比較"),
    DemoItem(Screen.AdvancedFlow,    "debounce / shareIn / stateIn / conflate / buffer"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("🚀 Coroutines & Flows", fontWeight = FontWeight.Bold)
                        Text(
                            "Kotlin 2026 面試完整攻略",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(demoItems) { item ->
                DemoCard(item = item, onClick = { onNavigate(item.screen) })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun DemoCard(item: DemoItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.screen.emoji,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.screen.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
