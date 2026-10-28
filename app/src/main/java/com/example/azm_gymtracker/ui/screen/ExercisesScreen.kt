package com.example.azm_gymtracker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import com.example.azm_gymtracker.ui.animation.azmPhysicsItemEntrance
import com.example.azm_gymtracker.ui.animation.azmPhysicsPress
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.azm_gymtracker.R
import com.example.azm_gymtracker.domain.model.Exercise
import com.example.azm_gymtracker.domain.model.MuscleCategory
import com.example.azm_gymtracker.ui.component.AzmBottomNavigation
import com.example.azm_gymtracker.ui.component.AzmNavTab
import com.example.azm_gymtracker.ui.theme.AzmBorderSubtle
import com.example.azm_gymtracker.ui.theme.AzmDarkBackground
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkOnSurfaceMuted
import com.example.azm_gymtracker.ui.theme.AzmDarkSurface
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceSelected
import com.example.azm_gymtracker.ui.theme.AzmDarkSurfaceVariant
import com.example.azm_gymtracker.ui.theme.AzmEmeraldGreen
import com.example.azm_gymtracker.ui.theme.AzmPrimaryGradient

/**
 * Exercise Library Screen (Figma Node #87:634 "مكتبة التمارين")
 * Categorized library with search, dual-row muscle filter chips, and custom exercise FAB.
 */
@Composable
fun ExercisesScreen(
    exercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    onCreateCustomExercise: (nameAr: String, nameEn: String, category: MuscleCategory) -> Unit,
    onTabSelected: (AzmNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<MuscleCategory?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var page by remember(searchQuery, selectedCategory) { mutableIntStateOf(1) }
    val pageSize = 15

    val filteredExercises = remember(exercises, searchQuery, selectedCategory) {
        exercises.filter { exercise ->
            val matchesCategory = selectedCategory == null || exercise.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                exercise.nameAr.contains(searchQuery, ignoreCase = true) ||
                exercise.nameEn.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val pagedExercises = remember(filteredExercises, page) {
        filteredExercises.take(page * pageSize)
    }
    val hasMore = pagedExercises.size < filteredExercises.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AzmBottomNavigation(
                currentTab = AzmNavTab.EXERCISES,
                onTabSelected = onTabSelected,
            )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .shadow(16.dp, CircleShape, spotColor = AzmEmeraldGreen.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(AzmPrimaryGradient)
                    .clickable { showCreateDialog = true }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.custom_exercise_fab),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF041C0E),
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            // Heading: "كل التمارين"
            Text(
                text = stringResource(R.string.exercises_library_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 4.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar (Height 48dp, Radius 16dp)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_exercise_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Row Muscle Filter Chips (Figma Node #87:664)
            val row1Categories = listOf(
                null to stringResource(R.string.picker_category_all),
                MuscleCategory.CHEST to stringResource(R.string.picker_category_chest),
                MuscleCategory.BACK to stringResource(R.string.picker_category_back),
                MuscleCategory.LEGS to stringResource(R.string.picker_category_legs),
                MuscleCategory.SHOULDERS to stringResource(R.string.picker_category_shoulders),
            )
            val row2Categories = listOf(
                MuscleCategory.ARMS to stringResource(R.string.picker_category_biceps),
                MuscleCategory.CORE to stringResource(R.string.picker_category_core),
                MuscleCategory.CARDIO to stringResource(R.string.picker_category_cardio),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row1Categories.forEach { (cat, label) ->
                    MuscleFilterChip(
                        label = label,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                row2Categories.forEach { (cat, label) ->
                    MuscleFilterChip(
                        label = label,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Count summary
            Text(
                text = "${filteredExercises.size} تمرينًا • جاهزة ومخصصة",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Exercise Cards List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(pagedExercises, key = { _, it -> it.id }) { index, exercise ->
                    ExerciseCardItem(
                        exercise = exercise,
                        onClick = { onSelectExercise(exercise) },
                        modifier = Modifier.azmPhysicsItemEntrance(index = index),
                    )
                }

                if (hasMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            com.example.azm_gymtracker.ui.component.AzmSecondaryButton(
                                text = "عرض المزيد (${filteredExercises.size - pagedExercises.size} تمرين متبقي)",
                                onClick = { page += 1 },
                                modifier = Modifier.fillMaxWidth(0.9f),
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp)) // Padding for Extended FAB
                }
            }
        }
    }

    if (showCreateDialog) {
        var nameArInput by remember { mutableStateOf("") }
        var nameEnInput by remember { mutableStateOf("") }
        var selectedCat by remember { mutableStateOf(MuscleCategory.CHEST) }
        val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = stringResource(R.string.picker_create_custom),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nameArInput,
                        onValueChange = { nameArInput = it },
                        label = { Text(stringResource(R.string.exercise_name_ar)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = nameEnInput,
                        onValueChange = { nameEnInput = it },
                        label = { Text(stringResource(R.string.exercise_name_en)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = stringResource(R.string.exercise_category_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MuscleCategory.entries.forEach { cat ->
                            val catName = when (cat) {
                                MuscleCategory.CHEST -> stringResource(R.string.picker_category_chest)
                                MuscleCategory.BACK -> stringResource(R.string.picker_category_back)
                                MuscleCategory.LEGS -> stringResource(R.string.picker_category_legs)
                                MuscleCategory.SHOULDERS -> stringResource(R.string.picker_category_shoulders)
                                MuscleCategory.ARMS -> stringResource(R.string.picker_category_arms)
                                MuscleCategory.CORE -> stringResource(R.string.picker_category_core)
                                MuscleCategory.CARDIO -> stringResource(R.string.picker_category_cardio)
                                MuscleCategory.OTHER -> stringResource(R.string.picker_category_all)
                            }
                            MuscleFilterChip(
                                label = catName,
                                isSelected = selectedCat == cat,
                                onClick = { selectedCat = cat },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (nameArInput.isNotBlank() || nameEnInput.isNotBlank()) {
                            onCreateCustomExercise(nameArInput.trim(), nameEnInput.trim(), selectedCat)
                            showCreateDialog = false
                        }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_save),
                        color = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }
}

@Composable
private fun MuscleFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
    val accent = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (isSelected) accent else MaterialTheme.colorScheme.outline
    val textCol = if (isSelected) accent else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(width = 1.dp, color = border, shape = CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textCol,
        )
    }
}

@Composable
private fun ExerciseCardItem(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = com.example.azm_gymtracker.ui.theme.LocalThemeIsDark.current
    val accent = if (isDark) AzmEmeraldGreen else MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(16.dp))
            .azmPhysicsPress(pressedScale = 0.96f, onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Start side / Right in RTL: Exercise Information
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        ) {
            Text(
                text = exercise.nameAr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = exercise.nameEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (exercise.isCustom) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.badge_custom),
                            style = MaterialTheme.typography.bodySmall,
                            color = accent,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }

        // End side / Left in RTL: Exercise thumbnail box
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🏋️",
                fontSize = 32.sp,
            )
        }
    }
}
