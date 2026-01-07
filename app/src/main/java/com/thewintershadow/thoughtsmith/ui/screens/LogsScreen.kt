/**
 * Logs screen for viewing application debug and event information.
 *
 * This file contains the UI for displaying in-memory application logs,
 * helping with debugging, user support, and understanding app behavior.
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
package com.thewintershadow.thoughtsmith.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thewintershadow.thoughtsmith.util.AppLogger

/**
 * Composable screen for displaying application logs.
 *
 * This screen provides a debug and monitoring interface that shows all logged events
 * from the application. It's useful for troubleshooting issues, understanding app behavior,
 * and providing support information.
 *
 * Features:
 * - Real-time log updates (refreshes every 500ms)
 * - Color-coded log levels (DEBUG, INFO, WARNING, ERROR)
 * - Clear logs functionality
 * - Reverse chronological order (newest first)
 * - Scrollable list of log entries
 * - Formatted timestamps and metadata
 *
 * The logs are stored in memory by AppLogger and are limited to the most recent
 * 1000 entries to prevent memory issues.
 *
 * @param onNavigateBack Callback invoked when the user navigates back from this screen
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(onNavigateBack: () -> Unit) {
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
                        fontSize = 20.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            AppLogger.clearLogs()
                            logs = AppLogger.getLogs()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Logs",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
    ) { paddingValues ->
        if (logs.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No logs available",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(logs.reversed()) { logEntry ->
                    LogEntryItem(logEntry = logEntry)
                }
            }
        }
    }
}

/**
 * Composable that renders a single log entry as a card.
 *
 * This component displays log metadata including level, tag, and message,
 * with color coding based on the log level for quick visual identification.
 *
 * Color Scheme:
 * - DEBUG: Gray (neutral, informational)
 * - INFO: Blue (primary color, normal operations)
 * - WARNING: Orange/Tertiary (attention needed)
 * - ERROR: Red (critical issues)
 *
 * The message is displayed in monospace font for better readability of
 * technical information, stack traces, and formatted data.
 *
 * @param logEntry The log entry to display, containing level, tag, message, and timestamp
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
@Composable
fun LogEntryItem(logEntry: AppLogger.LogEntry) {
    val backgroundColor =
        when (logEntry.level) {
            AppLogger.LogLevel.DEBUG -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            AppLogger.LogLevel.INFO -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            AppLogger.LogLevel.WARNING -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            AppLogger.LogLevel.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        }

    val textColor =
        when (logEntry.level) {
            AppLogger.LogLevel.DEBUG -> MaterialTheme.colorScheme.onSurfaceVariant
            AppLogger.LogLevel.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
            AppLogger.LogLevel.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
            AppLogger.LogLevel.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = backgroundColor,
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = logEntry.level.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Text(
                    text = logEntry.tag,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.7f),
                )
            }
            Text(
                text = logEntry.message,
                fontSize = 13.sp,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp,
            )
        }
    }
}
