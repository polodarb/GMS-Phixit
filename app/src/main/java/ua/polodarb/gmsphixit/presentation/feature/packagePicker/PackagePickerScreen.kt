package ua.polodarb.gmsphixit.presentation.feature.packagePicker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PackagePickerScreen(
    modifier: Modifier = Modifier,
    viewModel: PackagePickerViewModel,
    onClick: (packageName: String, appName: String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val state by viewModel::state
    val expandedSections = viewModel.expandedSections

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        topBar = {
            WarningBanner()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearch(it) },
                        placeholder = { Text("Search") },
                        shape = MaterialTheme.shapes.extraLarge,
                        trailingIcon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search"
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleManualInput()
                        },
                        shapes = IconButtonDefaults.shapes(pressedShape = IconButtonDefaults.mediumPressedShape),
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Outlined.Keyboard, contentDescription = "Add Manual Package")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSettingsClick()
                        },
                        shapes = IconButtonDefaults.shapes(pressedShape = IconButtonDefaults.mediumPressedShape),
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                }

                AnimatedVisibility(state.showManualInput) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Manual Package Input",
                                modifier = Modifier.padding(start = 16.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = state.manualPackageName,
                                onValueChange = { viewModel.onManualPackageChange(it) },
                                placeholder = { Text("Enter package name") },
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.extraLarge,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.toggleManualInput() }
                                ) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { viewModel.onManualPackageSubmit(onClick) },
                                    enabled = state.manualPackageName.isNotBlank(),
                                ) {
                                    Text("Open")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Box(Modifier.weight(1f)) {
                    when {
                        state.isLoading && state.sections.isEmpty() -> {
                            LoadingIndicator(
                                Modifier
                                    .padding(bottom = it.calculateTopPadding())
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .size(96.dp)
                            )
                        }

                        state.sections.isEmpty() && !state.isLoading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No packages found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp))
                            ) {
                                state.filteredSections.forEach { section ->
                                    item {
                                        ExpandableSectionCard(
                                            section = section,
                                            expanded = expandedSections.contains(section.packageName),
                                            onClick = { viewModel.toggleSection(section.packageName) },
                                            loading = viewModel.loadingSections.contains(section.packageName),
                                            onSelect = { packageName, appName ->
                                                onClick(packageName, appName)
                                            }
                                        )
                                        Spacer(Modifier.height(16.dp))
                                    }
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
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpandableSectionCard(
    section: AppSection,
    expanded: Boolean,
    onClick: () -> Unit,
    loading: Boolean,
    onSelect: (packageName: String, appName: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable { onClick() }
    ) {
        Column(Modifier.fillMaxWidth()) {
            SectionHeaderRow(section = section, expanded = expanded)
            if (expanded) {
                if (loading) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(16.dp)
                    ) {
                        LoadingIndicator(Modifier.align(Alignment.Center))
                    }
                } else {
                    SectionPackagesLazy(
                        packages = section.packages,
                        appName = section.appName,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(section: AppSection, expanded: Boolean) {
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, label = "arrowRotation")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (section.appIcon != null) {
            Image(
                bitmap = section.appIcon.toBitmap(48, 48).asImageBitmap(),
                contentDescription = section.appName,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = section.appName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .alpha(0.4f)
                .rotate(rotation)
        )
    }
}

@Composable
private fun SectionPackagesLazy(
    packages: List<String>,
    appName: String,
    onSelect: (packageName: String, appName: String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        packages.forEach { pkg ->
            PackageItem(pkg) {
                onSelect(pkg, appName)
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun PackageItem(
    pkg: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = pkg,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .alpha(0.4f)
            )
        }
    }
}
