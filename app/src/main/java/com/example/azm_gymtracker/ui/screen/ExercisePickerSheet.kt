package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.ui.theme.AzmBorderSubtle
import com.example.azm_gymtracker.ui.theme.AzmDarkBackground
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurfaceMuted
import com.example.azm_gymtracker.ui.theme.AzmDarkSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceSelected
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceVariant
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen

@Composable
private fun categoryDisplayName(category: MuscleCategory): String = when (category) {
    MuscleCategory.CHEST -> stringResource(R.string.picker_category_chest)
    MuscleCategory.BACK -> stringResource(R.string.picker_category_back)
    MuscleCategory.LEGS -> stringResource(R.string.picker_category_legs)
    MuscleCategory.SHOULDERS -> stringResource(R.string.picker_category_shoulders)
    MuscleCategory.ARMS -> stringResource(R.string.picker_category_arms)
    MuscleCategory.CORE -> stringResource(R.string.picker_category_core)
    MuscleCategory.CARDIO -> stringResource(R.string.picker_category_cardio)
    MuscleCategory.OTHER -> stringResource(R.string.picker_category_all)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerSheet(
    exercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    onCreateCustomExercise: (name: String, category: MuscleCategory) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MuscleCategory?>(null) }
    var showCustomDialog by remember { mutableStateOf(false) }

    val filteredExercises = remember(exercises, searchQuery, selectedCategory) {
        exercises.filter { ex ->
            val matchesCategory = selectedCategory == null || ex.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                ex.nameEn.contains(searchQuery, ignoreCase = true) ||
                ex.nameAr.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AzmDarkBackground,
        modifier = modifier.fillMaxHeight(0.9f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                )
                IconButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.picker_create_custom),
                        tint = AzmEmeraldGreen,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        stringResource(R.string.picker_search_hint),
                        color = AzmDarkOnSurfaceMuted,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = AzmDarkOnSurfaceMuted,
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AzmDarkSurface,
                    unfocusedContainerColor = AzmDarkSurface,
                    focusedBorderColor = AzmEmeraldGreen,
                    unfocusedBorderColor = AzmBorderSubtle,
                    focusedTextColor = AzmDarkOnSurface,
                    unfocusedTextColor = AzmDarkOnSurface,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Muscle Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text(stringResource(R.string.picker_category_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AzmEmeraldGreen,
                            selectedLabelColor = AzmDarkBackground,
                            containerColor = AzmDarkSurface,
                            labelColor = AzmDarkOnSurfaceMuted,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedCategory == null) AzmEmeraldGreen else AzmBorderSubtle,
                            selectedBorderColor = AzmEmeraldGreen,
                            enabled = true,
                            selected = selectedCategory == null,
                        ),
                    )
                }
                items(MuscleCategory.entries.toTypedArray()) { cat ->
                    val isSelected = selectedCategory == cat
                    val catName = categoryDisplayName(cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = if (isSelected) null else cat },
                        label = { Text(catName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AzmEmeraldGreen,
                            selectedLabelColor = AzmDarkBackground,
                            containerColor = AzmDarkSurface,
                            labelColor = AzmDarkOnSurfaceMuted,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) AzmEmeraldGreen else AzmBorderSubtle,
                            selectedBorderColor = AzmEmeraldGreen,
                            enabled = true,
                            selected = isSelected,
                        ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercises List
            if (filteredExercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.picker_empty_search),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzmDarkOnSurfaceMuted,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(filteredExercises, key = { it.id }) { exercise ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, AzmBorderSubtle, RoundedCornerShape(14.dp))
                                .clickable { onSelectExercise(exercise) },
                            colors = CardDefaults.cardColors(
                                containerColor = AzmDarkSurface,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = exercise.nameAr,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AzmDarkOnSurface,
                                    )
                                    Text(
                                        text = exercise.nameEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AzmDarkOnSurfaceMuted,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (exercise.isCustom) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(AzmDarkSurfaceVariant)
                                                .padding(horizontal = 8.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = stringResource(R.string.badge_custom),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AzmEmeraldGreen,
                                                fontSize = 10.sp,
                                            )
                                        }
                                    }

                                    Text(
                                        text = categoryDisplayName(exercise.category),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AzmDarkOnSurfaceMuted,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom Exercise Creation Dialog
    if (showCustomDialog) {
        var customName by remember { mutableStateOf("") }
        var customCategory by remember { mutableStateOf(MuscleCategory.CHEST) }

        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = AzmDarkSurface,
            title = {
                Text(
                    text = stringResource(R.string.picker_create_custom),
                    fontWeight = FontWeight.Bold,
                    color = AzmDarkOnSurface,
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text(stringResource(R.string.picker_custom_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzmEmeraldGreen,
                            unfocusedBorderColor = AzmBorderSubtle,
                            focusedTextColor = AzmDarkOnSurface,
                            unfocusedTextColor = AzmDarkOnSurface,
                        ),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.exercise_category_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = AzmDarkOnSurfaceMuted,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MuscleCategory.entries.toTypedArray()) { cat ->
                            FilterChip(
                                selected = customCategory == cat,
                                onClick = { customCategory = cat },
                                label = { Text(categoryDisplayName(cat), fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AzmEmeraldGreen,
                                    selectedLabelColor = AzmDarkBackground,
                                    containerColor = AzmDarkSurfaceVariant,
                                    labelColor = AzmDarkOnSurfaceMuted,
                                ),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            onCreateCustomExercise(customName.trim(), customCategory)
                            showCustomDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AzmEmeraldGreen,
                        contentColor = AzmDarkBackground,
                    ),
                ) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = AzmDarkOnSurfaceMuted)
                }
            },
        )
    }
}
