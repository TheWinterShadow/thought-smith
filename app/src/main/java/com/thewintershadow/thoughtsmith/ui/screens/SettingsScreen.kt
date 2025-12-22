package com.thewintershadow.thoughtsmith.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                            imageVector = Icons.Default.ArrowBack,
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
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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

            // View Logs button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "View application logs and debug information",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}
