package com.thewintershadow.thoughtsmith.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thewintershadow.thoughtsmith.data.Message
import com.thewintershadow.thoughtsmith.repository.FileStorageService
import com.thewintershadow.thoughtsmith.viewmodel.ChatViewModel
import com.thewintershadow.thoughtsmith.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel =
        viewModel(
            factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application),
        ),
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val fileStorageService = remember { FileStorageService(context) }

    // File picker launcher
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/markdown"),
        ) { uri ->
            if (uri != null && uiState.formattedSummary != null) {
                coroutineScope.launch {
                    val result =
                        fileStorageService.saveJournalEntryToUri(
                            uri = uri,
                            content = uiState.formattedSummary!!,
                        )
                    viewModel.onFileSaved(
                        success = result.isSuccess,
                        filePath = result.getOrNull(),
                    )
                }
            } else if (uri == null && uiState.isSaving) {
                // User cancelled file picker
                viewModel.onFileSaved(success = false)
            }
        }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // Show success snackbar
    LaunchedEffect(uiState.saveSuccess) {
        uiState.saveSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveSuccess()
        }
    }

    // Show file picker when user accepts summary
    LaunchedEffect(uiState.isSaving) {
        if (uiState.isSaving && uiState.formattedSummary != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH-mm-ss", Locale.getDefault())
            val timestamp = System.currentTimeMillis()
            val date = Date(timestamp)
            val fileName = "journal_entry_${dateFormat.format(date)}_${timeFormat.format(date)}.md"
            filePickerLauncher.launch(fileName)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Thought Smith",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        enabled = uiState.messages.size > 1, // More than just the welcome message
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint =
                                if (uiState.messages.size > 1) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                },
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { viewModel.saveJournalEntry() },
                            enabled = uiState.messages.isNotEmpty() && !uiState.isSaving && !uiState.isGeneratingSummary,
                        ) {
                            if (uiState.isGeneratingSummary) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Save Journal Entry",
                                    tint =
                                        if (uiState.messages.isNotEmpty() && !uiState.isSaving && !uiState.isGeneratingSummary) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                                        },
                                )
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
        ) {
            // Messages list
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(message = message)
                }

                if (uiState.isLoading) {
                    item {
                        LoadingIndicator()
                    }
                }
            }

            // Input/Output mode toggles
            InputOutputModeToggles(
                inputMode = uiState.inputMode,
                outputMode = uiState.outputMode,
                isListening = uiState.isListening,
                isSpeaking = uiState.isSpeaking,
                onToggleInputMode = { viewModel.toggleInputMode() },
                onToggleOutputMode = { viewModel.toggleOutputMode() },
                onStartListening = { viewModel.startListening() },
                onStopListening = { viewModel.stopListening() },
                onStopSpeaking = { viewModel.stopSpeaking() },
            )

            // Input field (only show for text mode)
            if (!uiState.inputMode) {
                MessageInputBar(
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    onSendClick = {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    },
                    enabled = !uiState.isLoading && messageText.isNotBlank(),
                )
            } else {
                // Speech input button
                SpeechInputBar(
                    isListening = uiState.isListening,
                    onStartListening = { viewModel.startListening() },
                    onStopListening = { viewModel.stopListening() },
                    enabled = !uiState.isLoading,
                )
            }
        }

        // Loading overlay when generating summary
        if (uiState.isGeneratingSummary && uiState.formattedSummary == null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "Generating Journal Entry...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Formatting your conversation into a beautiful journal entry",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }

        // Preview Dialog
        if (uiState.formattedSummary != null) {
            SummaryPreviewDialog(
                formattedContent = uiState.formattedSummary!!,
                isGenerating = uiState.isGeneratingSummary,
                onAccept = {
                    viewModel.acceptSummaryAndSave(uiState.formattedSummary!!)
                },
                onReject = {
                    viewModel.rejectSummary()
                },
            )
        }
    }
}

@Composable
fun SummaryPreviewDialog(
    formattedContent: String,
    isGenerating: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onReject,
        title = {
            Text(
                "Preview Journal Entry",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            )
        },
        text = {
            if (isGenerating) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator()
                        Text("Generating formatted summary...")
                    }
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = formattedContent,
                        modifier = Modifier.fillMaxWidth(),
                        style =
                            TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            ),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                enabled = !isGenerating,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onReject) {
                Text("Cancel")
            }
        },
        modifier = Modifier.fillMaxWidth(0.9f),
    )
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (message.isUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ).padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            val textColor =
                if (message.isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

            Text(
                text = message.content,
                modifier = Modifier.fillMaxWidth(),
                style =
                    TextStyle(
                        color = textColor,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    ),
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your thoughts...") },
                shape = RoundedCornerShape(24.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                    ),
                maxLines = 4,
            )

            IconButton(
                onClick = onSendClick,
                enabled = enabled,
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (enabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                        ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint =
                        if (enabled) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                )
            }
        }
    }
}

@Composable
fun InputOutputModeToggles(
    inputMode: Boolean,
    outputMode: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    onToggleInputMode: () -> Unit,
    onToggleOutputMode: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onStopSpeaking: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Input mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = if (inputMode) Icons.Default.Mic else Icons.Default.Edit,
                        contentDescription = if (inputMode) "Speech Input" else "Text Input",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Input Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (inputMode) "Voice input" else "Text input",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = inputMode,
                    onCheckedChange = { onToggleInputMode() },
                )
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )

            // Output mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = if (outputMode) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                        contentDescription = if (outputMode) "Speech Output" else "Text Output",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column {
                        Text(
                            text = "Output Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = if (outputMode) "Voice output" else "Text output",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(
                    checked = outputMode,
                    onCheckedChange = { onToggleOutputMode() },
                )
            }
        }
    }
}

@Composable
fun SpeechInputBar(
    isListening: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    enabled: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = if (isListening) onStopListening else onStartListening,
                enabled = enabled,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        if (isListening) {
                            MaterialTheme.colorScheme.error
                        } else if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop Listening" else "Start Listening",
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = if (isListening) "Listening... Tap to stop" else "Tap to start speaking",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
