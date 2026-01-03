package com.thewintershadow.thoughtsmith.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thewintershadow.thoughtsmith.data.AIProvider
import com.thewintershadow.thoughtsmith.data.TTSProvider
import com.thewintershadow.thoughtsmith.viewmodel.SettingsViewModel
import com.thewintershadow.thoughtsmith.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogs: () -> Unit,
    viewModel: SettingsViewModel =
        viewModel(
            factory = ViewModelFactory(LocalContext.current.applicationContext as android.app.Application),
        ),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                onClick = onNavigateToLogs,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "View App Logs",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "View application logs and debug information",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            // Tab Row
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Context") },
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Text API") },
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Speech") },
                )
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> ContextInfoTab(uiState, viewModel)
                1 -> TextAPITab(uiState, viewModel)
                2 -> SpeechTab(uiState, viewModel)
            }
        }
    }
}

@Composable
fun ContextInfoTab(
    uiState: com.thewintershadow.thoughtsmith.viewmodel.SettingsUiState,
    viewModel: SettingsViewModel,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // AI Context
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "AI Context",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    "Define how the AI should behave and what role it should take in your conversations.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )

                OutlinedTextField(
                    value = uiState.settings.aiContext,
                    onValueChange = { viewModel.updateAiContext(it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                    placeholder = { Text("Enter AI context instructions...") },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            }
        }

        // Output Format Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Output Format Instructions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    "Instructions for how the AI should format the journal entry when you save it.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )

                OutlinedTextField(
                    value = uiState.settings.outputFormatInstructions,
                    onValueChange = { viewModel.updateOutputFormat(it) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                    placeholder = { Text("Enter format instructions...") },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            }
        }

        // Save button
        Button(
            onClick = { viewModel.saveSettings() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    "Save Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (uiState.saveSuccess) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearSaveSuccess()
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Text(
                    "Settings saved successfully!",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
fun TextAPITab(
    uiState: com.thewintershadow.thoughtsmith.viewmodel.SettingsUiState,
    viewModel: SettingsViewModel,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // AI Provider Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "AI Provider",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                AIProvider.values().forEach { provider ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = uiState.settings.aiProvider == provider,
                            onClick = { viewModel.updateProvider(provider) },
                        )
                        Text(
                            text = provider.displayName,
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 16.sp,
                        )
                    }
                }
            }
        }

        // Model Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Model",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                uiState.availableModels.forEach { model ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = uiState.settings.aiModel == model.modelName,
                            onClick = { viewModel.updateModel(model.modelName) },
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = model.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = model.modelName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }
        }

        // API Key Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "API Key",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    "Enter your API key for the selected AI provider.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )

                OutlinedTextField(
                    value = uiState.settings.apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your API key") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            }
        }

        // Save button
        Button(
            onClick = { viewModel.saveSettings() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    "Save Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (uiState.saveSuccess) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearSaveSuccess()
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Text(
                    "Settings saved successfully!",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
fun SpeechTab(
    uiState: com.thewintershadow.thoughtsmith.viewmodel.SettingsUiState,
    viewModel: SettingsViewModel,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // TTS Provider Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Text-to-Speech Provider",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    "Choose your preferred TTS provider. Local is free and offline. Remote options provide more natural voices.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = uiState.settings.ttsProvider == TTSProvider.LOCAL,
                        onClick = { viewModel.updateTTSProvider(TTSProvider.LOCAL) },
                        label = { Text("Local (Device)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FilterChip(
                        selected = uiState.settings.ttsProvider == TTSProvider.OPENAI,
                        onClick = { viewModel.updateTTSProvider(TTSProvider.OPENAI) },
                        label = { Text("OpenAI TTS") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FilterChip(
                        selected = uiState.settings.ttsProvider == TTSProvider.ANTHROPIC,
                        onClick = { viewModel.updateTTSProvider(TTSProvider.ANTHROPIC) },
                        label = { Text("Anthropic TTS") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    FilterChip(
                        selected = uiState.settings.ttsProvider == TTSProvider.AWS_POLLY,
                        onClick = { viewModel.updateTTSProvider(TTSProvider.AWS_POLLY) },
                        label = { Text("AWS Polly") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                when (uiState.settings.ttsProvider) {
                    TTSProvider.LOCAL -> {
                        Text(
                            "Uses your device's built-in TTS engine. Free and works offline.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    TTSProvider.OPENAI, TTSProvider.ANTHROPIC -> {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            if (uiState.settings.ttsProvider == TTSProvider.ANTHROPIC) {
                                Text(
                                    "Note: Anthropic TTS API is not yet publicly available. " +
                                        "This option will be enabled when the API becomes available.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text(
                                    "Configure your TTS API settings. These are completely separate from your Text API configuration.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            // TTS Provider Type (OpenAI or Anthropic)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                    ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        "TTS Provider",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    listOf(AIProvider.OPENAI, AIProvider.ANTHROPIC).forEach { provider ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            RadioButton(
                                                selected = uiState.settings.ttsProviderType == provider,
                                                onClick = { viewModel.updateTTSProviderType(provider) },
                                            )
                                            Text(
                                                text = provider.displayName,
                                                modifier = Modifier.padding(start = 8.dp),
                                                fontSize = 14.sp,
                                            )
                                        }
                                    }
                                }
                            }

                            // TTS Model Selection
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                    ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        "TTS Model/Voice",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        if (uiState.settings.ttsProviderType == AIProvider.OPENAI) {
                                            "OpenAI TTS models: tts-1 (fast) or tts-1-hd (high quality)"
                                        } else {
                                            "Anthropic TTS model (when available)"
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                    OutlinedTextField(
                                        value = uiState.settings.ttsModel,
                                        onValueChange = { viewModel.updateTTSModel(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Model/Voice ID") },
                                        placeholder = { Text(if (uiState.settings.ttsProviderType == AIProvider.OPENAI) "e.g., tts-1 or tts-1-hd" else "Enter model name") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                            OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            ),
                                    )
                                }
                            }

                            // TTS API Key
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                    ),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        "TTS API Key",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        "Enter your API key for ${uiState.settings.ttsProviderType.displayName} TTS. " +
                                            "This is separate from your Text API key.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                    OutlinedTextField(
                                        value = uiState.settings.ttsApiKey,
                                        onValueChange = { viewModel.updateTTSApiKey(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("API Key") },
                                        placeholder = { Text("Enter TTS API key") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                            OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            ),
                                    )
                                }
                            }
                        }
                    }
                    TTSProvider.AWS_POLLY -> {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                "Note: Requires AWS credentials. Configure your AWS access key, secret key, and region below.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            
                            OutlinedTextField(
                                value = uiState.settings.awsAccessKey,
                                onValueChange = { viewModel.updateAWSAccessKey(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("AWS Access Key ID") },
                                placeholder = { Text("Enter AWS access key") },
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    ),
                            )
                            
                            OutlinedTextField(
                                value = uiState.settings.awsSecretKey,
                                onValueChange = { viewModel.updateAWSSecretKey(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("AWS Secret Access Key") },
                                placeholder = { Text("Enter AWS secret key") },
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    ),
                            )
                            
                            OutlinedTextField(
                                value = uiState.settings.awsRegion,
                                onValueChange = { viewModel.updateAWSRegion(it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("AWS Region") },
                                placeholder = { Text("e.g., us-east-1") },
                                shape = RoundedCornerShape(12.dp),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    ),
                            )
                        }
                    }
                }
            }
        }

        // Save button
        Button(
            onClick = { viewModel.saveSettings() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    "Save Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (uiState.saveSuccess) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearSaveSuccess()
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
            ) {
                Text(
                    "Settings saved successfully!",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}
