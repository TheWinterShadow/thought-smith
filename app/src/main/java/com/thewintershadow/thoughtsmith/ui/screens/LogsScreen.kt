package com.thewintershadow.thoughtsmith.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewintershadow.thoughtsmith.util.AppLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onNavigateBack: () -> Unit
) {
    var logs by remember { mutableStateOf(AppLogger.getLogs()) }
    
    LaunchedEffect(Unit) {
        // Refresh logs periodically
        while (true) {
            kotlinx.coroutines.delay(500)
            logs = AppLogger.getLogs()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "App Logs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            AppLogger.clearLogs()
                            logs = AppLogger.getLogs()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Logs"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No logs available",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs.reversed()) { logEntry ->
                    LogEntryItem(logEntry = logEntry)
                }
            }
        }
    }
}

@Composable
fun LogEntryItem(logEntry: AppLogger.LogEntry) {
    val backgroundColor = when (logEntry.level) {
        AppLogger.LogLevel.DEBUG -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        AppLogger.LogLevel.INFO -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        AppLogger.LogLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        AppLogger.LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }
    
    val textColor = when (logEntry.level) {
        AppLogger.LogLevel.DEBUG -> MaterialTheme.colorScheme.onSurfaceVariant
        AppLogger.LogLevel.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
        AppLogger.LogLevel.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        AppLogger.LogLevel.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = logEntry.level.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = logEntry.tag,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            Text(
                text = logEntry.message,
                fontSize = 13.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

