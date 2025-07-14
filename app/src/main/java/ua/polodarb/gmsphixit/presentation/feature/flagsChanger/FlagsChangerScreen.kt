package ua.polodarb.gmsphixit.presentation.feature.flagsChanger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ua.polodarb.gmsphixit.core.phixit.model.ParcelableFlagModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FlagsChangerScreen(
    viewModel: FlagsChangerViewModel,
    packageName: String,
    appName: String,
    onBack: () -> Unit
) {
    val state by viewModel::state
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(packageName) {
        viewModel.loadFlags(packageName)
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = appName,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBack()
                        },
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Flag")
            }
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .clip(RoundedCornerShape(32.dp, 32.dp, 0.dp, 0.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp)
            ) {
                TextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearch(it) },
                    placeholder = { Text("Search flags") },
                    shape = MaterialTheme.shapes.extraLarge,
                    trailingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .height(56.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                state.error?.let { error ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.weight(1f))
                            TextButton(
                                onClick = { viewModel.clearError() }
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                Box(Modifier.weight(1f)) {
                    when {
                        state.isLoading -> {
                            LoadingIndicator(
                                Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .size(96.dp)
                            )
                        }

                        state.filteredFlags.isEmpty() && !state.isLoading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (state.searchQuery.isBlank()) "No flags found" else "No flags match your search",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp)),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(
                                    items = state.filteredFlags
                                ) { flag ->
                                    FlagItem(
                                        flag = flag,
                                        onToggle = { newValue ->
                                            viewModel.updateFlag(flag.name, newValue)
                                        }
                                    )
                                }
                                item {
                                    Spacer(Modifier.navigationBarsPadding())
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.showAddDialog) {
            AddFlagDialog(
                flagName = state.newFlagName,
                flagValue = state.newFlagValue,
                onFlagNameChange = { viewModel.updateNewFlagName(it) },
                onFlagValueChange = { viewModel.updateNewFlagValue(it) },
                onConfirm = {
                    viewModel.addFlag(state.newFlagName, state.newFlagValue)
                },
                onDismiss = { viewModel.hideAddDialog() }
            )
        }
    }
}

@Composable
private fun FlagItem(
    flag: ParcelableFlagModel,
    onToggle: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val checked = (flag.value as? Boolean) == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = flag.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggle(it)
                }
            )
        }
    }
}

@Composable
private fun AddFlagDialog(
    flagName: String,
    flagValue: Boolean,
    onFlagNameChange: (String) -> Unit,
    onFlagValueChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Flag") },
        text = {
            Column {
                OutlinedTextField(
                    value = flagName,
                    shape = MaterialTheme.shapes.extraLarge,
                    onValueChange = onFlagNameChange,
                    label = { Text("Flag Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Value:", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = flagValue,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFlagValueChange(it)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = flagName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
