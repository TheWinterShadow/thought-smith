/**
 * Chat screen composables for the main conversational journaling interface.
 *
 * This file contains all UI components for the chat screen including the main
 * screen, message bubbles, input controls, speech controls, and dialogs.
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
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

/**
 * Main chat screen composable for the Thought Smith app.
 *
 * This is the primary interface where users interact with the AI assistant for journaling.
 * The screen manages the conversation flow, displays messages in a chat interface,
 * and provides controls for sending messages, toggling speech modes, and saving journal entries.
 *
 * Key Features:
 * - Real-time chat interface with user and AI messages
 * - Text and speech input modes (toggle between typing and voice)
 * - Text and speech output modes (read messages aloud)
 * - Auto-scrolling to latest messages
 * - Journal entry generation and preview
 * - File picker integration for saving journal entries
 * - Error and success notifications via Snackbar
 * - Clear chat functionality
 *
 * State Management:
 * The screen observes UI state from ChatViewModel using Kotlin Flow, providing
 * reactive updates for messages, loading states, errors, and mode toggles.
 *
 * User Flow:
 * 1. User sends messages via text input or voice
 * 2. AI responds with thoughtful questions and reflections
 * 3. Conversation continues with context maintained
 * 4. User can save conversation as formatted journal entry
 * 5. Preview generated entry and save to chosen location
 *
 * @param onNavigateToSettings Callback to navigate to settings screen
 * @param viewModel The ChatViewModel managing the chat state and logic
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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
                            enabled =
                                uiState.messages.isNotEmpty() &&
                                    !uiState.isSaving &&
                                    !uiState.isGeneratingSummary,
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
                                        if (uiState.messages.isNotEmpty() &&
                                            !uiState.isSaving &&
                                            !uiState.isGeneratingSummary
                                        ) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme
                                                .onPrimaryContainer
                                                .copy(alpha = 0.5f)
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

/**
 * Dialog for previewing and confirming the generated journal entry.
 *
 * This composable displays the AI-generated journal entry in a scrollable dialog,
 * allowing users to review the formatted content before saving it to a file.
 * Users can accept the entry (proceed to save) or reject it (return to chat).
 *
 * Features:
 * - Scrollable content area for long journal entries
 * - Loading indicator while generating
 * - Accept button to proceed with saving
 * - Cancel button to dismiss and return to editing
 *
 * @param formattedContent The AI-generated journal entry content to preview
 * @param isGenerating True if the content is still being generated
 * @param onAccept Callback when user accepts the journal entry
 * @param onReject Callback when user rejects/cancels the preview
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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

/**
 * Composable that displays a single message bubble in the chat interface.
 *
 * This component renders messages differently based on whether they're from
 * the user or the AI assistant:
 * - User messages: Right-aligned, primary color background
 * - AI messages: Left-aligned, surface variant background
 *
 * Design Features:
 * - Rounded corners for friendly appearance
 * - Maximum width constraint to maintain readability
 * - Appropriate text color contrast based on background
 * - Comfortable padding for touch targets
 *
 * @param message The Message object containing content, sender, and timestamp
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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

/**
 * Loading indicator displayed while waiting for AI response.
 *
 * Shows a small circular progress indicator in a message bubble-style container
 * aligned to the left side (where AI messages appear). This provides visual
 * feedback that the AI is processing and will respond soon.
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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

/**
 * Text input bar for typing messages.
 *
 * This composable provides a text field for typing messages along with a send button.
 * The input field expands to accommodate multiple lines and includes placeholder text.
 *
 * Features:
 * - Multi-line text input (up to 4 lines)
 * - Rounded corners for modern appearance
 * - Send button that's only enabled when there's text to send
 * - Visual feedback for enabled/disabled state
 * - Elevated surface to separate from content
 *
 * @param messageText Current text in the input field
 * @param onMessageTextChange Callback when text changes
 * @param onSendClick Callback when send button is clicked
 * @param enabled Whether the send button should be enabled
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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

/**
 * Toggle controls for switching between input and output modes.
 *
 * This component provides two switches that control:
 * 1. Input Mode: Text typing vs. Voice speech recognition
 * 2. Output Mode: Text display vs. Voice text-to-speech
 *
 * Each mode has:
 * - Clear icon indicating the current mode
 * - Label and description text
 * - Switch control for toggling
 *
 * Visual Design:
 * - Organized in two rows separated by a divider
 * - Icons and labels for quick comprehension
 * - Primary color accents for active states
 *
 * @param inputMode True for speech input, false for text input
 * @param outputMode True for speech output, false for text output
 * @param isListening True when actively listening for speech
 * @param isSpeaking True when text-to-speech is active
 * @param onToggleInputMode Callback to toggle input mode
 * @param onToggleOutputMode Callback to toggle output mode
 * @param onStartListening Callback to start listening (unused but kept for future enhancement)
 * @param onStopListening Callback to stop listening (unused but kept for future enhancement)
 * @param onStopSpeaking Callback to stop speaking (unused but kept for future enhancement)
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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
            modifier =
                Modifier
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
                        imageVector =
                            if (outputMode) {
                                Icons.AutoMirrored.Filled.VolumeUp
                            } else {
                                Icons.AutoMirrored.Filled.VolumeMute
                            },
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

/**
 * Speech input control with large microphone button.
 *
 * This composable provides a prominent microphone button for voice input,
 * replacing the text input bar when speech mode is active. The button changes
 * appearance based on whether the app is actively listening.
 *
 * Visual States:
 * - Not listening: Large blue button with "Tap to start speaking" label
 * - Listening: Large red button with "Listening... Tap to stop" label
 * - Disabled: Gray button with reduced opacity
 *
 * The button is intentionally large (64dp) for easy tapping and clear
 * visual prominence, making it obvious when voice input is active.
 *
 * @param isListening True when actively listening for speech
 * @param onStartListening Callback to start speech recognition
 * @param onStopListening Callback to stop speech recognition
 * @param enabled Whether the button should be enabled
 *
 * @author TheWinterShadow
 * @since 1.0.0
 */
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = if (isListening) onStopListening else onStartListening,
                enabled = enabled,
                modifier =
                    Modifier
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
                    tint =
                        if (enabled) {
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
