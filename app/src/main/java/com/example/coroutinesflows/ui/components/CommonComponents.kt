package com.example.coroutinesflows.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 通用 Scaffold 含 TopAppBar 及返回按鈕。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            content()
        }
    }
}

/** 程式碼說明 Card，用於顯示解釋文字。 */
@Composable
fun InfoCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 20.sp
        )
    }
}

/** 面試題 Card。 */
@Composable
fun InterviewCard(questions: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "💼 常見面試題 Interview Questions",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(6.dp))
            questions.forEach { q ->
                Text("• $q", style = MaterialTheme.typography.bodySmall, lineHeight = 18.sp)
                Spacer(Modifier.height(2.dp))
            }
        }
    }
}

/** 輸出日誌區塊，用等寬字型顯示日誌輸出。 */
@Composable
fun LogOutput(logs: List<String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "📋 輸出 Output",
                color = Color(0xFF4EC9B0),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(4.dp))
            if (logs.isEmpty()) {
                Text(
                    "按下按鈕開始 / Press a button to start",
                    color = Color(0xFF808080),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            } else {
                logs.forEach { line ->
                    Text(
                        line,
                        color = Color(0xFFD4D4D4),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/** 載入指示器。 */
@Composable
fun LoadingIndicator(message: String = "載入中… Loading…") {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(message)
    }
}

/** 錯誤顯示組件。 */
@Composable
fun ErrorMessage(message: String, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️ $message", color = MaterialTheme.colorScheme.error)
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) { Text("重試 Retry") }
        }
    }
}

/** 操作按鈕行。 */
@Composable
fun ActionRow(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onPrimary, modifier = Modifier.weight(1f)) { Text(primaryLabel) }
        if (secondaryLabel != null && onSecondary != null) {
            OutlinedButton(onClick = onSecondary, modifier = Modifier.weight(1f)) {
                Text(secondaryLabel)
            }
        }
    }
}
